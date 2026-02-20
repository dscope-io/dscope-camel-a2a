package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Part;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.persistence.core.RehydrationPolicy;
import io.dscope.camel.persistence.jdbc.JdbcFlowStateStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentA2APersistenceJdbcTest {

    @Test
    void taskRoundTripAcrossServiceInstances() {
        JdbcFlowStateStore store = newJdbcStore();

        PersistentA2ATaskEventService eventsFirst = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksFirst = new PersistentA2ATaskService(store, eventsFirst, RehydrationPolicy.DEFAULT);

        Task created = tasksFirst.sendMessage(newSendMessageRequest("jdbc-msg-1"));
        String taskId = created.getTaskId();
        assertNotNull(taskId);
        assertEquals(TaskState.RUNNING, created.getStatus().getState());

        PersistentA2ATaskEventService eventsSecond = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksSecond = new PersistentA2ATaskService(store, eventsSecond, RehydrationPolicy.DEFAULT);

        Task restored = tasksSecond.getTask(taskId);
        assertEquals(taskId, restored.getTaskId());
        assertEquals(TaskState.RUNNING, restored.getStatus().getState());

        List<?> restoredEvents = eventsSecond.readTaskEvents(taskId, 0L, 20);
        assertNotNull(restoredEvents);
    }

    @Test
    void taskContinuationWorksAfterSnapshotRehydrationAcrossInstances() {
        JdbcFlowStateStore store = newJdbcStore();
        RehydrationPolicy aggressiveSnapshots = new RehydrationPolicy(1, 500, 200);

        PersistentA2ATaskEventService eventsFirst = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksFirst = new PersistentA2ATaskService(store, eventsFirst, aggressiveSnapshots);
        Task created = tasksFirst.sendMessage(newSendMessageRequest("jdbc-msg-2"));

        PersistentA2ATaskEventService eventsSecond = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksSecond = new PersistentA2ATaskService(store, eventsSecond, aggressiveSnapshots);
        Task completed = tasksSecond.transitionTask(created.getTaskId(), TaskState.COMPLETED, "done-jdbc");
        assertEquals(TaskState.COMPLETED, completed.getStatus().getState());

        PersistentA2ATaskService tasksThird = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), aggressiveSnapshots);
        Task restored = tasksThird.getTask(created.getTaskId());
        assertEquals(TaskState.COMPLETED, restored.getStatus().getState());

        assertTrue(store.rehydrate("a2a.task", created.getTaskId()).envelope().snapshot().has("task"));
    }

    private JdbcFlowStateStore newJdbcStore() {
        String dbName = "a2aPersistence" + UUID.randomUUID().toString().replace("-", "");
        return new JdbcFlowStateStore("jdbc:derby:memory:" + dbName + ";create=true", "", "");
    }

    private SendMessageRequest newSendMessageRequest(String messageId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setRole("user");

        Part part = new Part();
        part.setPartId("p-" + messageId);
        part.setType("text");
        part.setText("hello " + messageId);
        message.setParts(List.of(part));

        SendMessageRequest request = new SendMessageRequest();
        request.setMessage(message);
        request.setIdempotencyKey("idem-" + messageId);
        return request;
    }
}
