package io.dscope.camel.a2a.model;

/**
 * Common task lifecycle states for A2A tasks.
 */
public enum TaskState {
    CREATED,
    QUEUED,
    RUNNING,
    WAITING,
    COMPLETED,
    FAILED,
    CANCELED
}
