package io.dscope.camel.a2a.model;

import java.util.Map;

/**
 * Current state and details of a task.
 */
public class TaskStatus {

    private TaskState state;
    private String message;
    private String updatedAt;
    private Map<String, Object> details;

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
