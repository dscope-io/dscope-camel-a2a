package io.dscope.camel.a2a.model;

import java.util.List;

/**
 * Advertised agent feature flags and method support.
 */
public class AgentCapabilities {

    private boolean streaming;
    private boolean pushNotifications;
    private boolean statefulTasks;
    private List<String> supportedMethods;

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

    public boolean isStatefulTasks() {
        return statefulTasks;
    }

    public void setStatefulTasks(boolean statefulTasks) {
        this.statefulTasks = statefulTasks;
    }

    public List<String> getSupportedMethods() {
        return supportedMethods;
    }

    public void setSupportedMethods(List<String> supportedMethods) {
        this.supportedMethods = supportedMethods;
    }
}
