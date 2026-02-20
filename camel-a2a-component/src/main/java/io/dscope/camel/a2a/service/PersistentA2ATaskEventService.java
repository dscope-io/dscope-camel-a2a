package io.dscope.camel.a2a.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.persistence.core.FlowStateStore;
import io.dscope.camel.persistence.core.PersistedEvent;
import io.dscope.camel.persistence.core.RehydratedState;
import io.dscope.camel.persistence.core.exception.OptimisticConflictException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent task event service built on top of the in-memory behavior.
 */
public class PersistentA2ATaskEventService extends InMemoryTaskEventService {

    private static final String FLOW_TYPE = "a2a.task";
    private static final int VERSION_READ_PAGE_SIZE = 500;

    private final FlowStateStore stateStore;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> hydrated = ConcurrentHashMap.newKeySet();
    private final ThreadLocal<Boolean> replaying = ThreadLocal.withInitial(() -> false);

    public PersistentA2ATaskEventService(FlowStateStore stateStore) {
        this.stateStore = stateStore;
    }

    @Override
    public void publishTaskUpdate(Task task) {
        super.publishTaskUpdate(task);
        if (Boolean.TRUE.equals(replaying.get()) || task == null || task.getTaskId() == null) {
            return;
        }

        TaskStatus status = task.getStatus();
        if (status == null || status.getState() == null) {
            return;
        }

        PersistedEvent event = new PersistedEvent(
            UUID.randomUUID().toString(),
            FLOW_TYPE,
            task.getTaskId(),
            0,
            "task.status",
            mapper.valueToTree(Map.of(
                "taskId", task.getTaskId(),
                "state", status.getState().name(),
                "message", status.getMessage() == null ? "" : status.getMessage(),
                "timestamp", status.getUpdatedAt() == null ? Instant.now().toString() : status.getUpdatedAt()
            )),
            Instant.now().toString(),
            null
        );

        long expectedVersion = resolveCurrentVersion(task.getTaskId());
        try {
            stateStore.appendEvents(FLOW_TYPE, task.getTaskId(), expectedVersion, List.of(event), null);
        } catch (OptimisticConflictException conflict) {
            long actual = resolveCurrentVersion(task.getTaskId());
            stateStore.appendEvents(FLOW_TYPE, task.getTaskId(), actual, List.of(event), null);
        }
    }

    @Override
    public List<TaskEvent> readTaskEvents(String taskId, long afterSequence, int limit) {
        ensureHydrated(taskId);
        return super.readTaskEvents(taskId, afterSequence, limit);
    }

    @Override
    public boolean isTaskTerminal(String taskId) {
        ensureHydrated(taskId);
        return super.isTaskTerminal(taskId);
    }

    private void ensureHydrated(String taskId) {
        if (taskId == null || taskId.isBlank() || !hydrated.add(taskId)) {
            return;
        }
        RehydratedState rehydrated = stateStore.rehydrate(FLOW_TYPE, taskId);
        List<PersistedEvent> events = rehydrated.tailEvents();
        if (events == null || events.isEmpty()) {
            return;
        }

        replaying.set(true);
        try {
            for (PersistedEvent event : events) {
                JsonNode payload = event.payload();
                if (payload == null || payload.get("state") == null) {
                    continue;
                }
                Task task = new Task();
                task.setTaskId(taskId);
                TaskStatus status = new TaskStatus();
                status.setState(TaskState.valueOf(payload.get("state").asText()));
                status.setMessage(payload.path("message").asText(""));
                status.setUpdatedAt(payload.path("timestamp").asText(Instant.now().toString()));
                task.setStatus(status);
                super.publishTaskUpdate(task);
            }
        } finally {
            replaying.set(false);
        }
    }

    private long resolveCurrentVersion(String taskId) {
        long afterSequence = 0L;
        long maxSequence = 0L;

        while (true) {
            List<PersistedEvent> page = stateStore.readEvents(FLOW_TYPE, taskId, afterSequence, VERSION_READ_PAGE_SIZE);
            if (page == null || page.isEmpty()) {
                return maxSequence;
            }

            for (PersistedEvent persistedEvent : page) {
                maxSequence = Math.max(maxSequence, persistedEvent.sequence());
            }

            afterSequence = maxSequence;
            if (page.size() < VERSION_READ_PAGE_SIZE) {
                return maxSequence;
            }
        }
    }
}
