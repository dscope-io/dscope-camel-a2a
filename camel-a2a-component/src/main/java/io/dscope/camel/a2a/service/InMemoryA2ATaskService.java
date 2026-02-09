package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.processor.A2AIllegalTaskStateException;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Simple in-memory task service for core protocol method execution.
 */
public class InMemoryA2ATaskService implements A2ATaskService {

    private final A2ATaskEventPublisher eventPublisher;
    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<TaskStatus>> historyByTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> idempotencyToTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> taskLocks = new ConcurrentHashMap<>();

    private static final Map<TaskState, Set<TaskState>> ALLOWED_TRANSITIONS = Map.of(
        TaskState.CREATED, EnumSet.of(TaskState.QUEUED, TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.QUEUED, EnumSet.of(TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.RUNNING, EnumSet.of(TaskState.WAITING, TaskState.COMPLETED, TaskState.FAILED, TaskState.CANCELED),
        TaskState.WAITING, EnumSet.of(TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.COMPLETED, EnumSet.noneOf(TaskState.class),
        TaskState.FAILED, EnumSet.noneOf(TaskState.class),
        TaskState.CANCELED, EnumSet.noneOf(TaskState.class)
    );

    public InMemoryA2ATaskService() {
        this(new NoopTaskEventPublisher());
    }

    public InMemoryA2ATaskService(A2ATaskEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher == null ? new NoopTaskEventPublisher() : eventPublisher;
    }

    @Override
    public Task sendMessage(SendMessageRequest request) {
        Message message = request.getMessage();
        if (message == null) {
            throw new A2AInvalidParamsException("SendMessage requires message");
        }

        String idempotencyKey = normalizeIdempotencyKey(request);
        synchronized (idempotencyToTaskId) {
            if (idempotencyKey != null) {
                String existingTaskId = idempotencyToTaskId.get(idempotencyKey);
                if (existingTaskId != null) {
                    return getTask(existingTaskId);
                }
            }

            String now = Instant.now().toString();
            String taskId = UUID.randomUUID().toString();

            TaskStatus status = new TaskStatus();
            status.setState(TaskState.CREATED);
            status.setMessage("Task created");
            status.setUpdatedAt(now);

            Task task = new Task();
            task.setTaskId(taskId);
            task.setStatus(status);
            task.setLatestMessage(message);
            task.setMessages(List.of(message));
            task.setArtifacts(new ArrayList<>());
            task.setMetadata(request.getMetadata());
            task.setCreatedAt(now);
            task.setUpdatedAt(now);

            Object lock = taskLocks.computeIfAbsent(taskId, ignored -> new Object());
            synchronized (lock) {
                tasks.put(taskId, task);
                historyByTaskId.put(taskId, new ArrayList<>(List.of(copyStatus(status))));
                eventPublisher.publishTaskUpdate(task);
                if (idempotencyKey != null) {
                    idempotencyToTaskId.putIfAbsent(idempotencyKey, taskId);
                }
                transitionTask(taskId, TaskState.RUNNING, "Task created from SendMessage");
                return tasks.get(taskId);
            }
        }
    }

    @Override
    public Task getTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new A2AInvalidParamsException("Task not found: " + taskId);
        }
        return task;
    }

    @Override
    public List<Task> listTasks(ListTasksRequest request) {
        List<Task> current = new ArrayList<>(tasks.values());
        if (request != null && request.getState() != null && !request.getState().isBlank()) {
            String expectedState = request.getState().toUpperCase();
            current = current.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getState() != null)
                .filter(t -> t.getStatus().getState().name().equals(expectedState))
                .collect(Collectors.toList());
        }
        if (request != null && request.getLimit() != null && request.getLimit() > 0 && current.size() > request.getLimit()) {
            return current.subList(0, request.getLimit());
        }
        return current;
    }

    @Override
    public Task cancelTask(CancelTaskRequest request) {
        if (request == null || request.getTaskId() == null || request.getTaskId().isBlank()) {
            throw new A2AInvalidParamsException("CancelTask requires taskId");
        }
        String reason = request.getReason() == null || request.getReason().isBlank() ? "Task canceled" : request.getReason();
        return transitionTask(request.getTaskId(), TaskState.CANCELED, reason);
    }

    @Override
    public Task transitionTask(String taskId, TaskState targetState, String reason) {
        if (taskId == null || taskId.isBlank()) {
            throw new A2AInvalidParamsException("taskId is required");
        }
        if (targetState == null) {
            throw new A2AInvalidParamsException("targetState is required");
        }

        Task task = getTask(taskId);
        Object lock = taskLocks.computeIfAbsent(taskId, ignored -> new Object());
        synchronized (lock) {
            TaskStatus currentStatus = task.getStatus();
            TaskState currentState = currentStatus == null ? null : currentStatus.getState();
            if (currentState == null) {
                currentState = TaskState.CREATED;
            }

            if (currentState == targetState) {
                return task;
            }

            Set<TaskState> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentState, Set.of());
            if (!allowedTargets.contains(targetState)) {
                throw new A2AIllegalTaskStateException(
                    "Illegal task transition: " + currentState + " -> " + targetState);
            }

            String now = Instant.now().toString();
            TaskStatus next = new TaskStatus();
            next.setState(targetState);
            next.setMessage(reason == null || reason.isBlank() ? "State changed to " + targetState : reason);
            next.setUpdatedAt(now);
            if (currentStatus != null) {
                next.setDetails(currentStatus.getDetails());
            }

            task.setStatus(next);
            task.setUpdatedAt(now);
            historyByTaskId.computeIfAbsent(taskId, ignored -> new ArrayList<>()).add(copyStatus(next));
            tasks.put(taskId, task);
            eventPublisher.publishTaskUpdate(task);
            return task;
        }
    }

    @Override
    public List<TaskStatus> getTaskHistory(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new A2AInvalidParamsException("taskId is required");
        }
        Object lock = taskLocks.computeIfAbsent(taskId, ignored -> new Object());
        List<TaskStatus> history = historyByTaskId.get(taskId);
        synchronized (lock) {
            if (history == null) {
                throw new A2AInvalidParamsException("Task not found: " + taskId);
            }
            return history.stream().map(this::copyStatus).collect(Collectors.toList());
        }
    }

    private String normalizeIdempotencyKey(SendMessageRequest request) {
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            return "request:" + request.getIdempotencyKey().trim();
        }
        Message message = request.getMessage();
        if (message != null && message.getMessageId() != null && !message.getMessageId().isBlank()) {
            return "message:" + message.getMessageId().trim();
        }
        return null;
    }

    private TaskStatus copyStatus(TaskStatus status) {
        TaskStatus copy = new TaskStatus();
        copy.setState(status.getState());
        copy.setMessage(status.getMessage());
        copy.setUpdatedAt(status.getUpdatedAt());
        copy.setDetails(status.getDetails());
        return copy;
    }
}
