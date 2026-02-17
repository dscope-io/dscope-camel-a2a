package io.dscope.camel.a2a.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.processor.A2AIllegalTaskStateException;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;
import io.dscope.camel.persistence.core.FlowStateStore;
import io.dscope.camel.persistence.core.RehydrationPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Persistent implementation of A2ATaskService. Keeps in-memory cache while backing state with FlowStateStore.
 */
public class PersistentA2ATaskService implements A2ATaskService {

    private static final String TASK_FLOW = "a2a.task";
    private static final String META_FLOW = "a2a.meta";
    private static final String META_ID = "global";

    private final A2ATaskEventPublisher eventPublisher;
    private final FlowStateStore stateStore;
    private final RehydrationPolicy policy;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<TaskStatus>> historyByTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> idempotencyToTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> taskLocks = new ConcurrentHashMap<>();
    private final Set<String> knownTaskIds = ConcurrentHashMap.newKeySet();

    private static final Map<TaskState, Set<TaskState>> ALLOWED_TRANSITIONS = Map.of(
        TaskState.CREATED, EnumSet.of(TaskState.QUEUED, TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.QUEUED, EnumSet.of(TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.RUNNING, EnumSet.of(TaskState.WAITING, TaskState.COMPLETED, TaskState.FAILED, TaskState.CANCELED),
        TaskState.WAITING, EnumSet.of(TaskState.RUNNING, TaskState.CANCELED, TaskState.FAILED),
        TaskState.COMPLETED, EnumSet.noneOf(TaskState.class),
        TaskState.FAILED, EnumSet.noneOf(TaskState.class),
        TaskState.CANCELED, EnumSet.noneOf(TaskState.class)
    );

    public PersistentA2ATaskService(FlowStateStore stateStore, A2ATaskEventPublisher eventPublisher, RehydrationPolicy policy) {
        this.stateStore = stateStore;
        this.eventPublisher = eventPublisher == null ? new NoopTaskEventPublisher() : eventPublisher;
        this.policy = policy == null ? RehydrationPolicy.DEFAULT : policy;
        loadMeta();
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
                knownTaskIds.add(taskId);
                eventPublisher.publishTaskUpdate(task);
                if (idempotencyKey != null) {
                    idempotencyToTaskId.putIfAbsent(idempotencyKey, taskId);
                }
                persistTask(taskId);
                persistMeta();
                transitionTask(taskId, TaskState.RUNNING, "Task created from SendMessage");
                return tasks.get(taskId);
            }
        }
    }

    @Override
    public Task getTask(String taskId) {
        ensureTaskLoaded(taskId);
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new A2AInvalidParamsException("Task not found: " + taskId);
        }
        return task;
    }

    @Override
    public List<Task> listTasks(ListTasksRequest request) {
        for (String taskId : knownTaskIds) {
            ensureTaskLoaded(taskId);
        }

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
                throw new A2AIllegalTaskStateException("Illegal task transition: " + currentState + " -> " + targetState);
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
            knownTaskIds.add(taskId);
            eventPublisher.publishTaskUpdate(task);
            persistTask(taskId);
            return task;
        }
    }

    @Override
    public List<TaskStatus> getTaskHistory(String taskId) {
        ensureTaskLoaded(taskId);
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

    private void ensureTaskLoaded(String taskId) {
        if (taskId == null || taskId.isBlank() || tasks.containsKey(taskId)) {
            return;
        }
        JsonNode snapshot = stateStore.rehydrate(TASK_FLOW, taskId).envelope().snapshot();
        if (snapshot == null || snapshot.isMissingNode() || snapshot.isEmpty()) {
            return;
        }
        Task task = mapper.convertValue(snapshot.path("task"), Task.class);
        List<TaskStatus> history = mapper.convertValue(snapshot.path("history"), new TypeReference<List<TaskStatus>>() {});
        if (task != null) {
            tasks.put(taskId, task);
            historyByTaskId.put(taskId, history == null ? new ArrayList<>() : new ArrayList<>(history));
            knownTaskIds.add(taskId);
        }
    }

    private void loadMeta() {
        JsonNode snapshot = stateStore.rehydrate(META_FLOW, META_ID).envelope().snapshot();
        if (snapshot == null || snapshot.isMissingNode() || snapshot.isEmpty()) {
            return;
        }

        List<String> taskIds = mapper.convertValue(snapshot.path("taskIds"), new TypeReference<List<String>>() {});
        if (taskIds != null) {
            knownTaskIds.addAll(taskIds);
        }

        Map<String, String> idempotencyMap = mapper.convertValue(snapshot.path("idempotencyToTaskId"), new TypeReference<Map<String, String>>() {});
        if (idempotencyMap != null) {
            idempotencyToTaskId.putAll(idempotencyMap);
        }
    }

    private void persistMeta() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskIds", new ArrayList<>(knownTaskIds));
        payload.put("idempotencyToTaskId", new HashMap<>(idempotencyToTaskId));
        stateStore.writeSnapshot(META_FLOW, META_ID, knownTaskIds.size(), mapper.valueToTree(payload), Map.of("updatedAt", Instant.now().toString()));
    }

    private void persistTask(String taskId) {
        Task task = tasks.get(taskId);
        List<TaskStatus> history = historyByTaskId.get(taskId);
        if (task == null || history == null) {
            return;
        }

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("task", task);
        snapshot.put("history", history);
        stateStore.writeSnapshot(TASK_FLOW, taskId, history.size(), mapper.valueToTree(snapshot), Map.of("updatedAt", Instant.now().toString()));
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
