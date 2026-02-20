package io.dscope.camel.a2a.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.persistence.core.AppendResult;
import io.dscope.camel.persistence.core.FlowStateStore;
import io.dscope.camel.persistence.core.PersistedEvent;
import io.dscope.camel.persistence.core.RehydratedState;
import io.dscope.camel.persistence.core.StateEnvelope;
import io.dscope.camel.persistence.core.exception.OptimisticConflictException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentA2ATaskEventServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void publishTaskUpdateSkipsInvalidTaskInputs() {
        FakeFlowStateStore store = new FakeFlowStateStore();
        PersistentA2ATaskEventService service = new PersistentA2ATaskEventService(store);

        service.publishTaskUpdate(null);

        Task missingId = new Task();
        missingId.setTaskId(null);
        missingId.setStatus(status(TaskState.RUNNING, "running"));
        service.publishTaskUpdate(missingId);

        Task missingStatus = new Task();
        missingStatus.setTaskId("task-1");
        missingStatus.setStatus(null);
        service.publishTaskUpdate(missingStatus);

        Task missingState = new Task();
        missingState.setTaskId("task-1");
        TaskStatus status = new TaskStatus();
        status.setMessage("no state");
        missingState.setStatus(status);
        service.publishTaskUpdate(missingState);

        assertEquals(0, store.appendAttempts);
    }

    @Test
    void publishTaskUpdateRetriesOnOptimisticConflict() {
        FakeFlowStateStore store = new FakeFlowStateStore();
        store.forceConflictOnFirstAppend = true;
        store.persistedEvents.add(persistedEvent(1, "task-2", TaskState.CREATED, "created"));

        PersistentA2ATaskEventService service = new PersistentA2ATaskEventService(store);
        Task task = task("task-2", TaskState.RUNNING, "running");

        service.publishTaskUpdate(task);

        assertEquals(2, store.appendAttempts);
        assertEquals(List.of(1L, 1L), store.expectedVersions);
        assertEquals(2, store.persistedEvents.size());
    }

    @Test
    void readTaskEventsHydratesTailAndMarksTerminal() {
        FakeFlowStateStore store = new FakeFlowStateStore();
        store.rehydrateTailEvents = List.of(
            persistedEvent(1, "task-3", TaskState.RUNNING, "running"),
            persistedEvent(2, "task-3", TaskState.COMPLETED, "done")
        );

        PersistentA2ATaskEventService service = new PersistentA2ATaskEventService(store);

        assertEquals(2, service.readTaskEvents("task-3", 0, 20).size());
        assertTrue(service.isTaskTerminal("task-3"));
        assertEquals(0, store.appendAttempts);
    }

    private static Task task(String taskId, TaskState state, String message) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setStatus(status(state, message));
        return task;
    }

    private static TaskStatus status(TaskState state, String message) {
        TaskStatus status = new TaskStatus();
        status.setState(state);
        status.setMessage(message);
        status.setUpdatedAt(Instant.now().toString());
        return status;
    }

    private static PersistedEvent persistedEvent(long sequence, String taskId, TaskState state, String message) {
        return new PersistedEvent(
            "evt-" + sequence,
            "a2a.task",
            taskId,
            sequence,
            "task.status",
            MAPPER.valueToTree(Map.of(
                "taskId", taskId,
                "state", state.name(),
                "message", message,
                "timestamp", Instant.now().toString()
            )),
            Instant.now().toString(),
            null
        );
    }

    private static final class FakeFlowStateStore implements FlowStateStore {
        private final List<PersistedEvent> persistedEvents = new ArrayList<>();
        private List<PersistedEvent> rehydrateTailEvents = List.of();
        private boolean forceConflictOnFirstAppend;
        private int appendAttempts;
        private final List<Long> expectedVersions = new ArrayList<>();

        @Override
        public RehydratedState rehydrate(String flowType, String flowId) {
            JsonNode snapshot = MAPPER.valueToTree(Map.of("task", Map.of("taskId", flowId)));
            StateEnvelope envelope = new StateEnvelope(flowType, flowId, persistedEvents.size(), 0, snapshot, Instant.now().toString(), Map.of());
            return new RehydratedState(envelope, rehydrateTailEvents);
        }

        @Override
        public AppendResult appendEvents(String flowType, String flowId, long expectedVersion, List<PersistedEvent> events, String idempotencyKey) {
            appendAttempts++;
            expectedVersions.add(expectedVersion);

            if (forceConflictOnFirstAppend && appendAttempts == 1) {
                throw new OptimisticConflictException("forced conflict");
            }

            long next = persistedEvents.stream().mapToLong(PersistedEvent::sequence).max().orElse(0L);
            for (PersistedEvent event : events) {
                next++;
                persistedEvents.add(new PersistedEvent(
                    event.eventId(),
                    event.flowType(),
                    event.flowId(),
                    next,
                    event.eventType(),
                    event.payload(),
                    event.occurredAt(),
                    event.idempotencyKey()
                ));
            }

            return new AppendResult(expectedVersion, next, false);
        }

        @Override
        public void writeSnapshot(String flowType, String flowId, long snapshotVersion, JsonNode snapshot, Map<String, Object> metadata) {
        }

        @Override
        public List<PersistedEvent> readEvents(String flowType, String flowId, long afterSequence, int limit) {
            return persistedEvents.stream()
                .filter(event -> event.sequence() > afterSequence)
                .sorted((left, right) -> Long.compare(left.sequence(), right.sequence()))
                .limit(limit)
                .toList();
        }
    }
}
