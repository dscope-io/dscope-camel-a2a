package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for ListPushNotificationConfigs.
 */
public class ListPushNotificationConfigsRequest {

    private String taskId;
    private Integer limit;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
