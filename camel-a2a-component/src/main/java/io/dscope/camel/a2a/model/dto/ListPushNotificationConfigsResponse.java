package io.dscope.camel.a2a.model.dto;

import java.util.List;
import io.dscope.camel.a2a.model.PushNotificationConfig;

/**
 * Result payload for ListPushNotificationConfigs.
 */
public class ListPushNotificationConfigsResponse {

    private List<PushNotificationConfig> configs;

    public List<PushNotificationConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<PushNotificationConfig> configs) {
        this.configs = configs;
    }
}
