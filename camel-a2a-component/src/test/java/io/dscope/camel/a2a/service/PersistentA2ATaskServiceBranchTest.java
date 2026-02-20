package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Part;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.processor.A2AIllegalTaskStateException;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;
import io.dscope.camel.persistence.jdbc.JdbcFlowStateStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentA2ATaskServiceBranchTest {

    @Test
    void sendMessageWithoutMessageThrowsInvalidParams() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, null, null);
        SendMessageRequest request = new SendMessageRequest();

        A2AInvalidParamsException error = assertThrows(A2AInvalidParamsException.class, () -> service.sendMessage(request));
        assertEquals("SendMessage requires message", error.getMessage());
    }

    @Test
    void cancelTaskWithoutTaskIdThrowsInvalidParams() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, null, null);

        A2AInvalidParamsException error = assertThrows(A2AInvalidParamsException.class, () -> service.cancelTask(new CancelTaskRequest()));
        assertEquals("CancelTask requires taskId", error.getMessage());
    }

    @Test
    void transitionToSameStateIsNoop() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskEventService events = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, events, null);

        Task task = service.sendMessage(newSendMessageRequest("noop-msg"));
        int historyBefore = service.getTaskHistory(task.getTaskId()).size();

        Task result = service.transitionTask(task.getTaskId(), TaskState.RUNNING, "still running");

        assertEquals(TaskState.RUNNING, result.getStatus().getState());
        assertEquals(historyBefore, service.getTaskHistory(task.getTaskId()).size());
    }

    @Test
    void transitionFromTerminalStateIsRejected() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);

        Task task = service.sendMessage(newSendMessageRequest("terminal-msg"));
        service.transitionTask(task.getTaskId(), TaskState.COMPLETED, "done");

        A2AIllegalTaskStateException error = assertThrows(
            A2AIllegalTaskStateException.class,
            () -> service.transitionTask(task.getTaskId(), TaskState.RUNNING, "illegal")
        );
        assertTrue(error.getMessage().contains("Illegal task transition"));
    }

    @Test
    void listTasksAppliesLimitAfterFiltering() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);

        Task t1 = service.sendMessage(newSendMessageRequest("limit-1"));
        Task t2 = service.sendMessage(newSendMessageRequest("limit-2"));
        service.transitionTask(t2.getTaskId(), TaskState.COMPLETED, "done");

        ListTasksRequest allWithLimit = new ListTasksRequest();
        allWithLimit.setLimit(1);
        List<Task> limited = service.listTasks(allWithLimit);
        assertEquals(1, limited.size());

        ListTasksRequest runningOnly = new ListTasksRequest();
        runningOnly.setState("RUNNING");
        List<Task> running = service.listTasks(runningOnly);
        assertEquals(1, running.size());
        assertEquals(t1.getTaskId(), running.get(0).getTaskId());
    }

    @Test
    void getTaskHistoryBlankTaskIdThrowsInvalidParams() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);

        A2AInvalidParamsException error = assertThrows(A2AInvalidParamsException.class, () -> service.getTaskHistory(""));
        assertEquals("taskId is required", error.getMessage());
    }

    @Test
    void defaultsAreAppliedWhenPublisherAndPolicyAreNull() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, null, null);

        Task created = service.sendMessage(newSendMessageRequest("defaults-msg"));
        assertNotNull(created.getTaskId());
        assertEquals(TaskState.RUNNING, created.getStatus().getState());
        List<TaskStatus> history = service.getTaskHistory(created.getTaskId());
        assertEquals(2, history.size());
    }

    @Test
    void sendMessageIsIdempotentAcrossInstancesViaPersistedMeta() {
        JdbcFlowStateStore store = newJdbcStore();

        PersistentA2ATaskService first = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);
        Task created = first.sendMessage(newSendMessageRequest("idem-shared", "idem-shared-key"));

        PersistentA2ATaskService second = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);
        Task reused = second.sendMessage(newSendMessageRequest("idem-shared-ignored", "idem-shared-key"));

        assertEquals(created.getTaskId(), reused.getTaskId());
        assertEquals(TaskState.RUNNING, second.getTask(created.getTaskId()).getStatus().getState());
    }

    @Test
    void sendMessageFallsBackToMessageIdWhenIdempotencyKeyMissing() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);

        Task first = service.sendMessage(newSendMessageRequest("message-idem", null));
        Task second = service.sendMessage(newSendMessageRequest("message-idem", null));

        assertEquals(first.getTaskId(), second.getTaskId());
    }

    @Test
    void cancelTaskBlankReasonUsesDefaultMessage() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);
        Task task = service.sendMessage(newSendMessageRequest("cancel-default", "idem-cancel-default"));

        CancelTaskRequest request = new CancelTaskRequest();
        request.setTaskId(task.getTaskId());
        request.setReason("   ");

        Task canceled = service.cancelTask(request);
        assertEquals(TaskState.CANCELED, canceled.getStatus().getState());
        assertEquals("Task canceled", canceled.getStatus().getMessage());
    }

    @Test
    void transitionTaskRejectsNullTargetState() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);
        Task task = service.sendMessage(newSendMessageRequest("null-target", "idem-null-target"));

        A2AInvalidParamsException error = assertThrows(
            A2AInvalidParamsException.class,
            () -> service.transitionTask(task.getTaskId(), null, "missing target")
        );
        assertEquals("targetState is required", error.getMessage());
    }

    @Test
    void unknownTaskLookupsThrowInvalidParams() {
        JdbcFlowStateStore store = newJdbcStore();
        PersistentA2ATaskService service = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), null);

        A2AInvalidParamsException getTaskError = assertThrows(A2AInvalidParamsException.class, () -> service.getTask("missing-task"));
        assertEquals("Task not found: missing-task", getTaskError.getMessage());

        A2AInvalidParamsException historyError = assertThrows(A2AInvalidParamsException.class, () -> service.getTaskHistory("missing-task"));
        assertEquals("Task not found: missing-task", historyError.getMessage());
    }

    private JdbcFlowStateStore newJdbcStore() {
        String dbName = "a2aBranch" + UUID.randomUUID().toString().replace("-", "");
        return new JdbcFlowStateStore("jdbc:derby:memory:" + dbName + ";create=true", "", "");
    }

    private SendMessageRequest newSendMessageRequest(String messageId) {
        return newSendMessageRequest(messageId, "idem-" + messageId);
    }

    private SendMessageRequest newSendMessageRequest(String messageId, String idempotencyKey) {
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
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }
}
