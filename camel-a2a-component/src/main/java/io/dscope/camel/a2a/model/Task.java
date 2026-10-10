package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * A2A task aggregate root.
 */
public class Task {

    private String taskId;
    private String contextId;
    private TaskStatus status;
    private Message latestMessage;
    private List<Message> messages;
    private List<Artifact> artifacts;
    private Map<String, Object> metadata;
    private String createdAt;
    private String updatedAt;

    @JsonProperty("id")
    @JsonAlias("taskId")
    public String getTaskId() {
        return taskId;
    }

    @JsonProperty("id")
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public Message getLatestMessage() {
        return latestMessage;
    }

    @JsonIgnore
    public void setLatestMessage(Message latestMessage) {
        this.latestMessage = latestMessage;
    }

    @JsonProperty("history")
    @JsonAlias("messages")
    public List<Message> getMessages() {
        return messages;
    }

    @JsonProperty("history")
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @JsonIgnore
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonIgnore
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
