package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.PushNotificationConfig;

/**
 * Result payload for GetPushNotificationConfig.
 */
public class GetPushNotificationConfigResponse {

    private PushNotificationConfig config;

    public PushNotificationConfig getConfig() {
        return config;
    }

    public void setConfig(PushNotificationConfig config) {
        this.config = config;
    }
}
