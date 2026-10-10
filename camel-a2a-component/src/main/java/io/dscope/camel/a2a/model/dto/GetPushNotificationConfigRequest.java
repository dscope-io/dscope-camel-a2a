package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for GetPushNotificationConfig.
 */
public class GetPushNotificationConfigRequest {

    private String configId;
    private String taskId;
    private String tenant;

    @JsonProperty("id")
    @JsonAlias("configId")
    public String getConfigId() {
        return configId;
    }

    @JsonProperty("id")
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
