package io.dscope.camel.a2a.model.dto;

/**
 * Result payload for DeletePushNotificationConfig.
 */
public class DeletePushNotificationConfigResponse {

    private String configId;
    private boolean deleted;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
