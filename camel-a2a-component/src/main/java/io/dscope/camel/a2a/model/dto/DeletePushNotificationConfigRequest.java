package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for DeletePushNotificationConfig.
 */
public class DeletePushNotificationConfigRequest {

    private String configId;

    @JsonProperty("id")
    @JsonAlias("configId")
    public String getConfigId() {
        return configId;
    }

    @JsonProperty("id")
    public void setConfigId(String configId) {
        this.configId = configId;
    }
}
