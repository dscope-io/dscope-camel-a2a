package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * Advertised agent feature flags and method support.
 */
public class AgentCapabilities {

    private boolean streaming;
    private boolean pushNotifications;
    private boolean statefulTasks;
    private List<String> supportedMethods;
    private boolean extendedAgentCard;

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean isPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    @JsonIgnore
    public boolean isStatefulTasks() {
        return statefulTasks;
    }

    @JsonIgnore
    public void setStatefulTasks(boolean statefulTasks) {
        this.statefulTasks = statefulTasks;
    }

    @JsonIgnore
    public List<String> getSupportedMethods() {
        return supportedMethods;
    }

    @JsonIgnore
    public void setSupportedMethods(List<String> supportedMethods) {
        this.supportedMethods = supportedMethods;
    }

    public boolean isExtendedAgentCard() {
        return extendedAgentCard;
    }

    public void setExtendedAgentCard(boolean extendedAgentCard) {
        this.extendedAgentCard = extendedAgentCard;
    }
}
