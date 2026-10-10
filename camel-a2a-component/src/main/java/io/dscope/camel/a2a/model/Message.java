package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * A task-associated message exchanged between participants.
 */
public class Message {

    private String messageId;
    private String role;
    private String contextId;
    private String taskId;
    private List<String> extensions;
    private List<String> referenceTaskIds;
    private String inReplyTo;
    private List<Part> parts;
    private Map<String, Object> metadata;
    private String createdAt;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @JsonIgnore
    public String getRole() {
        return role;
    }

    @JsonIgnore
    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("role")
    public String getProtocolRole() {
        if ("assistant".equalsIgnoreCase(role) || "agent".equalsIgnoreCase(role) || "ROLE_AGENT".equals(role)) {
            return "ROLE_AGENT";
        }
        if ("user".equalsIgnoreCase(role) || "ROLE_USER".equals(role)) {
            return "ROLE_USER";
        }
        return role;
    }

    @JsonProperty("role")
    public void setProtocolRole(String role) {
        if ("ROLE_AGENT".equals(role) || "agent".equalsIgnoreCase(role)) {
            this.role = "assistant";
        } else if ("ROLE_USER".equals(role)) {
            this.role = "user";
        } else {
            this.role = role;
        }
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @JsonIgnore
    public String getInReplyTo() {
        return inReplyTo;
    }

    @JsonIgnore
    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
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
}
    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getReferenceTaskIds() {
        return referenceTaskIds;
    }

    public void setReferenceTaskIds(List<String> referenceTaskIds) {
        this.referenceTaskIds = referenceTaskIds;
    }
