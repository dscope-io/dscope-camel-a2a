package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Task;

/**
 * No-op event publisher used when streaming support is not wired.
 */
public class NoopTaskEventPublisher implements A2ATaskEventPublisher {

    @Override
    public void publishTaskUpdate(Task task) {
        // Intentionally empty.
    }
}
