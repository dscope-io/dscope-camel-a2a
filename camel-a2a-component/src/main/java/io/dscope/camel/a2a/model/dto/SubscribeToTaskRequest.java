package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for SubscribeToTask.
 */
public class SubscribeToTaskRequest {

    private String taskId;
    private Long afterSequence;
    private Integer limit;

    @JsonProperty("id")
    @JsonAlias("taskId")
    public String getTaskId() {
        return taskId;
    }

    @JsonProperty("id")
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
