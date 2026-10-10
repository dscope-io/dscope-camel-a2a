package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for GetTask.
 */
public class GetTaskRequest {

    private String taskId;
    private Integer historyLength;
    private String tenant;

    @JsonProperty("id")
    @JsonAlias("taskId")
    public String getTaskId() {
        return taskId;
    }

    @JsonProperty("id")
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getHistoryLength() {
        return historyLength;
    }

    public void setHistoryLength(Integer historyLength) {
        this.historyLength = historyLength;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
