package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.TaskSubscription;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * In-memory task event publisher and subscription registry.
 */
public class InMemoryTaskEventService implements A2ATaskEventPublisher {

    private static final Set<TaskState> TERMINAL_STATES = Set.of(TaskState.COMPLETED, TaskState.CANCELED, TaskState.FAILED);

    private final ConcurrentMap<String, AtomicLong> sequenceByTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<TaskEvent>> eventsByTaskId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TaskSubscription> subscriptionsById = new ConcurrentHashMap<>();
    private final List<Consumer<TaskEvent>> listeners = new ArrayList<>();
    private final int maxEventsPerTask;

    public InMemoryTaskEventService() {
        this(256);
    }

    public InMemoryTaskEventService(int maxEventsPerTask) {
        this.maxEventsPerTask = Math.max(16, maxEventsPerTask);
    }

    @Override
    public void publishTaskUpdate(Task task) {
        if (task == null || task.getTaskId() == null || task.getTaskId().isBlank()) {
            return;
        }
        TaskStatus status = task.getStatus();
        if (status == null || status.getState() == null) {
            return;
        }

        String taskId = task.getTaskId();
        long sequence = sequenceByTaskId.computeIfAbsent(taskId, ignored -> new AtomicLong()).incrementAndGet();

        TaskEvent event = new TaskEvent();
        event.setTaskId(taskId);
        event.setSequence(sequence);
        event.setEventType("task.status");
        event.setState(status.getState());
        event.setMessage(status.getMessage());
        event.setTimestamp(status.getUpdatedAt() != null ? status.getUpdatedAt() : Instant.now().toString());
        event.setTerminal(TERMINAL_STATES.contains(status.getState()));
        event.setPayload(Map.of(
            "taskId", taskId,
            "state", status.getState().name(),
            "message", status.getMessage() == null ? "" : status.getMessage()
        ));

        List<TaskEvent> events = eventsByTaskId.computeIfAbsent(taskId, ignored -> new ArrayList<>());
        synchronized (events) {
            events.add(copyEvent(event));
            if (events.size() > maxEventsPerTask) {
                events.subList(0, events.size() - maxEventsPerTask).clear();
            }
        }

        if (event.isTerminal()) {
            markSubscriptionsTerminal(taskId);
        }
        notifyListeners(event);
    }

    public TaskSubscription createSubscription(String taskId, long afterSequence) {
        if (taskId == null || taskId.isBlank()) {
            throw new A2AInvalidParamsException("SubscribeToTask requires taskId");
        }
        String now = Instant.now().toString();
        TaskSubscription subscription = new TaskSubscription();
        subscription.setSubscriptionId(UUID.randomUUID().toString());
        subscription.setTaskId(taskId);
        subscription.setAfterSequence(Math.max(afterSequence, 0L));
        subscription.setLastDeliveredSequence(Math.max(afterSequence, 0L));
        subscription.setTerminal(isTaskTerminal(taskId));
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);
        subscriptionsById.put(subscription.getSubscriptionId(), subscription);
        return copySubscription(subscription);
    }

    public List<TaskEvent> readTaskEvents(String taskId, long afterSequence, int limit) {
        if (taskId == null || taskId.isBlank()) {
            throw new A2AInvalidParamsException("taskId is required");
        }
        int resolvedLimit = limit <= 0 ? 100 : Math.min(limit, 500);
        List<TaskEvent> events = eventsByTaskId.get(taskId);
        if (events == null) {
            return List.of();
        }
        synchronized (events) {
            return events.stream()
                .filter(e -> e.getSequence() > afterSequence)
                .sorted(Comparator.comparingLong(TaskEvent::getSequence))
                .limit(resolvedLimit)
                .map(this::copyEvent)
                .collect(Collectors.toList());
        }
    }

    public void acknowledgeSubscription(String subscriptionId, long lastDeliveredSequence, boolean terminal) {
        if (subscriptionId == null || subscriptionId.isBlank()) {
            return;
        }
        TaskSubscription subscription = subscriptionsById.get(subscriptionId);
        if (subscription == null) {
            return;
        }
        subscription.setLastDeliveredSequence(Math.max(subscription.getLastDeliveredSequence(), lastDeliveredSequence));
        subscription.setUpdatedAt(Instant.now().toString());
        if (terminal) {
            subscription.setTerminal(true);
        }
    }

    public TaskSubscription getSubscription(String subscriptionId) {
        TaskSubscription subscription = subscriptionsById.get(subscriptionId);
        return subscription == null ? null : copySubscription(subscription);
    }

    public boolean isTaskTerminal(String taskId) {
        List<TaskEvent> events = eventsByTaskId.get(taskId);
        if (events == null) {
            return false;
        }
        synchronized (events) {
            return !events.isEmpty() && events.get(events.size() - 1).isTerminal();
        }
    }

    public void cleanupTerminalSubscriptions() {
        subscriptionsById.values().removeIf(TaskSubscription::isTerminal);
    }

    public int getActiveSubscriptionCount() {
        int count = 0;
        for (TaskSubscription sub : subscriptionsById.values()) {
            if (!sub.isTerminal()) {
                count++;
            }
        }
        return count;
    }

    public int getBufferedEventCount() {
        int total = 0;
        for (List<TaskEvent> events : eventsByTaskId.values()) {
            synchronized (events) {
                total += events.size();
            }
        }
        return total;
    }

    public void addListener(Consumer<TaskEvent> listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void markSubscriptionsTerminal(String taskId) {
        subscriptionsById.values().forEach(sub -> {
            if (taskId.equals(sub.getTaskId())) {
                sub.setTerminal(true);
                sub.setUpdatedAt(Instant.now().toString());
            }
        });
    }

    private void notifyListeners(TaskEvent event) {
        List<Consumer<TaskEvent>> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }
        for (Consumer<TaskEvent> listener : snapshot) {
            listener.accept(copyEvent(event));
        }
    }

    private TaskEvent copyEvent(TaskEvent source) {
        TaskEvent copy = new TaskEvent();
        copy.setSequence(source.getSequence());
        copy.setTaskId(source.getTaskId());
        copy.setEventType(source.getEventType());
        copy.setState(source.getState());
        copy.setMessage(source.getMessage());
        copy.setTimestamp(source.getTimestamp());
        copy.setTerminal(source.isTerminal());
        copy.setPayload(source.getPayload());
        return copy;
    }

    private TaskSubscription copySubscription(TaskSubscription source) {
        TaskSubscription copy = new TaskSubscription();
        copy.setSubscriptionId(source.getSubscriptionId());
        copy.setTaskId(source.getTaskId());
        copy.setAfterSequence(source.getAfterSequence());
        copy.setLastDeliveredSequence(source.getLastDeliveredSequence());
        copy.setTerminal(source.isTerminal());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }
}
