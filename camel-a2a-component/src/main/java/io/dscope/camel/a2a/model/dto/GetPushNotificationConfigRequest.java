package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for GetPushNotificationConfig.
 */
public class GetPushNotificationConfigRequest {

    private String configId;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }
}
