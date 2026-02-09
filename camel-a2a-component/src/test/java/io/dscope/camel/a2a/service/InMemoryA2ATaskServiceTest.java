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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryA2ATaskServiceTest {

    @Test
    void sendMessageCreatesTaskAndHistory() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        SendMessageRequest request = sendMessageRequest("msg-1", null);

        Task task = service.sendMessage(request);
        List<?> history = service.getTaskHistory(task.getTaskId());

        assertEquals(TaskState.RUNNING, task.getStatus().getState());
        assertEquals(2, history.size()); // CREATED -> RUNNING
    }

    @Test
    void sendMessageIsIdempotentForRepeatedRequestId() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();

        SendMessageRequest first = sendMessageRequest("msg-a", "req-1");
        SendMessageRequest second = sendMessageRequest("msg-b", "req-1");

        Task t1 = service.sendMessage(first);
        Task t2 = service.sendMessage(second);

        assertEquals(t1.getTaskId(), t2.getTaskId());
    }

    @Test
    void sendMessageIsIdempotentForRepeatedMessageId() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();

        SendMessageRequest first = sendMessageRequest("same-message-id", null);
        SendMessageRequest second = sendMessageRequest("same-message-id", null);

        Task t1 = service.sendMessage(first);
        Task t2 = service.sendMessage(second);

        assertEquals(t1.getTaskId(), t2.getTaskId());
    }

    @Test
    void transitionRejectsIllegalTerminalMove() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        Task task = service.sendMessage(sendMessageRequest("msg-term", null));

        service.transitionTask(task.getTaskId(), TaskState.COMPLETED, "done");
        A2AIllegalTaskStateException error = assertThrows(
            A2AIllegalTaskStateException.class,
            () -> service.transitionTask(task.getTaskId(), TaskState.RUNNING, "resume"));

        assertEquals("Illegal task transition: COMPLETED -> RUNNING", error.getMessage());
    }

    @Test
    void cancelTaskRejectsTerminalState() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        Task task = service.sendMessage(sendMessageRequest("msg-cancel", null));
        service.transitionTask(task.getTaskId(), TaskState.COMPLETED, "complete");

        CancelTaskRequest cancel = new CancelTaskRequest();
        cancel.setTaskId(task.getTaskId());
        cancel.setReason("user request");

        A2AIllegalTaskStateException error = assertThrows(A2AIllegalTaskStateException.class, () -> service.cancelTask(cancel));
        assertEquals("Illegal task transition: COMPLETED -> CANCELED", error.getMessage());
    }

    @Test
    void listTasksFiltersByState() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        Task running = service.sendMessage(sendMessageRequest("m1", "r1"));
        Task completed = service.sendMessage(sendMessageRequest("m2", "r2"));
        service.transitionTask(completed.getTaskId(), TaskState.COMPLETED, "done");

        ListTasksRequest request = new ListTasksRequest();
        request.setState("RUNNING");
        List<Task> runningTasks = service.listTasks(request);

        assertTrue(runningTasks.stream().anyMatch(t -> t.getTaskId().equals(running.getTaskId())));
        assertTrue(runningTasks.stream().noneMatch(t -> t.getTaskId().equals(completed.getTaskId())));
    }

    @Test
    void concurrentIdempotentSendMessageReturnsSingleTaskId() throws Exception {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                calls.add(() -> service.sendMessage(sendMessageRequest("msg-parallel", "idem-key")).getTaskId());
            }

            Set<String> taskIds = pool.invokeAll(calls).stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(java.util.stream.Collectors.toSet());

            assertEquals(1, taskIds.size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void getTaskHistoryForUnknownTaskThrows() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        A2AInvalidParamsException error = assertThrows(
            A2AInvalidParamsException.class,
            () -> service.getTaskHistory("missing-task"));
        assertEquals("Task not found: missing-task", error.getMessage());
    }

    @Test
    void lifecyclePathProducesDeterministicStateHistory() {
        InMemoryA2ATaskService service = new InMemoryA2ATaskService();
        Task task = service.sendMessage(sendMessageRequest("msg-life", null));

        service.transitionTask(task.getTaskId(), TaskState.WAITING, "waiting for dependency");
        service.transitionTask(task.getTaskId(), TaskState.RUNNING, "dependency resolved");
        service.transitionTask(task.getTaskId(), TaskState.COMPLETED, "complete");

        List<TaskStatus> history = service.getTaskHistory(task.getTaskId());
        List<TaskState> states = history.stream().map(TaskStatus::getState).toList();

        assertEquals(List.of(
            TaskState.CREATED,
            TaskState.RUNNING,
            TaskState.WAITING,
            TaskState.RUNNING,
            TaskState.COMPLETED
        ), states);
    }

    private SendMessageRequest sendMessageRequest(String messageId, String idempotencyKey) {
        Part part = new Part();
        part.setPartId("p-" + messageId);
        part.setType("text");
        part.setText("hello");

        Message message = new Message();
        message.setMessageId(messageId);
        message.setRole("user");
        message.setParts(List.of(part));

        SendMessageRequest request = new SendMessageRequest();
        request.setMessage(message);
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }
}
