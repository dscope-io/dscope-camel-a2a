package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for GetTask.
 */
public class GetTaskRequest {

    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
