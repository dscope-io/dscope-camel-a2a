package io.dscope.camel.a2a.model;

/**
 * Subscription metadata for task event streams.
 */
public class TaskSubscription {

    private String subscriptionId;
    private String taskId;
    private long afterSequence;
    private long lastDeliveredSequence;
    private boolean terminal;
    private String createdAt;
    private String updatedAt;

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

    public long getLastDeliveredSequence() {
        return lastDeliveredSequence;
    }

    public void setLastDeliveredSequence(long lastDeliveredSequence) {
        this.lastDeliveredSequence = lastDeliveredSequence;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
