package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Common task lifecycle states for A2A tasks.
 */
public enum TaskState {
    UNSPECIFIED,
    SUBMITTED,
    WORKING,
    INPUT_REQUIRED,
    AUTH_REQUIRED,
    REJECTED,
    // Internal legacy states retained for API and persistence compatibility.
    CREATED,
    QUEUED,
    RUNNING,
    WAITING,
    COMPLETED,
    FAILED,
    CANCELED;

    @JsonValue
    public String toProtocolValue() {
        return switch (this) {
            case CREATED, QUEUED -> "TASK_STATE_SUBMITTED";
            case RUNNING -> "TASK_STATE_WORKING";
            case WAITING -> "TASK_STATE_INPUT_REQUIRED";
            default -> "TASK_STATE_" + name();
        };
    }

    @JsonCreator
    public static TaskState fromProtocolValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.startsWith("TASK_STATE_") ? value.substring("TASK_STATE_".length()) : value;
        return switch (normalized) {
            case "SUBMITTED" -> CREATED;
            case "WORKING" -> RUNNING;
            case "INPUT_REQUIRED" -> WAITING;
            case "AUTH_REQUIRED" -> AUTH_REQUIRED;
            case "REJECTED" -> REJECTED;
            case "COMPLETED" -> COMPLETED;
            case "FAILED" -> FAILED;
            case "CANCELED", "CANCELLED" -> CANCELED;
            case "UNSPECIFIED" -> UNSPECIFIED;
            case "CREATED" -> CREATED;
            case "QUEUED" -> QUEUED;
            case "RUNNING" -> RUNNING;
            case "WAITING" -> WAITING;
            default -> throw new IllegalArgumentException("Unknown A2A task state: " + value);
        };
    }
}
