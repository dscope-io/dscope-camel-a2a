package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for SendStreamingMessage.
 */
public class SendStreamingMessageResponse {

    private Task task;
    private String subscriptionId;
    private String streamUrl;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
