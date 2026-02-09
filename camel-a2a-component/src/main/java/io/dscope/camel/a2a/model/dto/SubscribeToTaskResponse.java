package io.dscope.camel.a2a.model.dto;

/**
 * Result payload for SubscribeToTask.
 */
public class SubscribeToTaskResponse {

    private String subscriptionId;
    private String taskId;
    private long afterSequence;
    private String streamUrl;
    private boolean terminal;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getAfterSequence() {
        return afterSequence;
    }

    public void setAfterSequence(long afterSequence) {
        this.afterSequence = afterSequence;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }
}
