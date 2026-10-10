package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Parameters for CancelTask.
 */
public class CancelTaskRequest {

    private String taskId;
    private String reason;
    private Map<String, Object> metadata;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
