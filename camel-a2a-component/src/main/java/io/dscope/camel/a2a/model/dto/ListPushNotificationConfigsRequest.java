package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for ListPushNotificationConfigs.
 */
public class ListPushNotificationConfigsRequest {

    private String taskId;
    private Integer limit;
    private String pageToken;
    private String tenant;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @JsonProperty("pageSize")
    @JsonAlias("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("pageSize")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
