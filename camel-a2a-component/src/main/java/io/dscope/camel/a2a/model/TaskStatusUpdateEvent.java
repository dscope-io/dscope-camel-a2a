package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A protocol status update for a task stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatusUpdateEvent {

    private String taskId;
    private String contextId;
    private TaskStatus status;
    private Boolean isFinal;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("final")
    public Boolean getFinal() {
        return isFinal;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("final")
    public void setFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
