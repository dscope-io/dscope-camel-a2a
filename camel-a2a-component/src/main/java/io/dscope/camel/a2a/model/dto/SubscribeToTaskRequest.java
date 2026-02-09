package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for SubscribeToTask.
 */
public class SubscribeToTaskRequest {

    private String taskId;
    private Long afterSequence;
    private Integer limit;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getAfterSequence() {
        return afterSequence;
    }

    public void setAfterSequence(Long afterSequence) {
        this.afterSequence = afterSequence;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
