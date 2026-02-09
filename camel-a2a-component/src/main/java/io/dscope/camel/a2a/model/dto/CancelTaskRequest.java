package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for CancelTask.
 */
public class CancelTaskRequest {

    private String taskId;
    private String reason;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
