package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Task;

/**
 * Publishes task update events for streaming consumers.
 */
public interface A2ATaskEventPublisher {

    void publishTaskUpdate(Task task);
}
