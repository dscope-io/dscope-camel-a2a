package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.TaskSubscription;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskEventServiceTest {

    @Test
    void publishAndReadEventsPreservesOrder() {
        InMemoryTaskEventService service = new InMemoryTaskEventService();

        service.publishTaskUpdate(task("task-1", TaskState.CREATED, "created"));
        service.publishTaskUpdate(task("task-1", TaskState.RUNNING, "running"));
        service.publishTaskUpdate(task("task-1", TaskState.WAITING, "waiting"));

        List<TaskEvent> events = service.readTaskEvents("task-1", 0, 20);
        assertEquals(3, events.size());
        assertEquals(1, events.get(0).getSequence());
        assertEquals(2, events.get(1).getSequence());
        assertEquals(3, events.get(2).getSequence());
        assertEquals(TaskState.WAITING, events.get(2).getState());
    }

    @Test
    void terminalStateMarksSubscriptionAndCanBeCleanedUp() {
        InMemoryTaskEventService service = new InMemoryTaskEventService();
        service.publishTaskUpdate(task("task-2", TaskState.RUNNING, "running"));
        TaskSubscription sub = service.createSubscription("task-2", 0);
        assertFalse(sub.isTerminal());

        service.publishTaskUpdate(task("task-2", TaskState.COMPLETED, "done"));
        TaskSubscription stored = service.getSubscription(sub.getSubscriptionId());
        assertNotNull(stored);
        assertTrue(stored.isTerminal());

        service.cleanupTerminalSubscriptions();
        assertNull(service.getSubscription(sub.getSubscriptionId()));
    }

    private Task task(String taskId, TaskState state, String message) {
        Task task = new Task();
        task.setTaskId(taskId);
        TaskStatus status = new TaskStatus();
        status.setState(state);
        status.setMessage(message);
        status.setUpdatedAt("2026-02-09T00:00:00Z");
        task.setStatus(status);
        return task;
    }
}
