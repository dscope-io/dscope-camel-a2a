package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for ListTasks.
 */
public class ListTasksRequest {

    private Integer limit;
    private String cursor;
    private String state;
    private String contextId;
    private Integer historyLength;
    private Boolean includeArtifacts;

    @JsonProperty("pageSize")
    @JsonAlias("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("pageSize")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("pageToken")
    @JsonAlias("cursor")
    public String getCursor() {
        return cursor;
    }

    @JsonProperty("pageToken")
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    @JsonProperty("status")
    @JsonAlias("state")
    public String getState() {
        return state;
    }

    @JsonProperty("status")
    public void setState(String state) {
        if (state == null) {
            this.state = null;
        } else {
            String normalized = state.startsWith("TASK_STATE_") ? state.substring("TASK_STATE_".length()) : state;
            this.state = switch (normalized) {
                case "SUBMITTED" -> "CREATED";
                case "WORKING" -> "RUNNING";
                case "INPUT_REQUIRED", "AUTH_REQUIRED" -> "WAITING";
                default -> normalized;
            };
        }
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Integer getHistoryLength() {
        return historyLength;
    }

    public void setHistoryLength(Integer historyLength) {
        this.historyLength = historyLength;
    }

    public Boolean getIncludeArtifacts() {
        return includeArtifacts;
    }

    public void setIncludeArtifacts(Boolean includeArtifacts) {
        this.includeArtifacts = includeArtifacts;
    }
}
