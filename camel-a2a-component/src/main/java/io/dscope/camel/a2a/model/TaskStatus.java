package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

/**
 * Current state and details of a task.
 */
public class TaskStatus {

    private TaskState state;
    private String message;
    private String updatedAt;
    private Map<String, Object> details;

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    @JsonIgnore
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("message")
    public Message getProtocolMessage() {
        if (message == null) {
            return null;
        }
        Message statusMessage = new Message();
        statusMessage.setMessageId(java.util.UUID.randomUUID().toString());
        statusMessage.setRole("assistant");
        Part textPart = new Part();
        textPart.setText(message);
        statusMessage.setParts(java.util.List.of(textPart));
        return statusMessage;
    }

    @JsonProperty("message")
    public void setProtocolMessage(Message statusMessage) {
        if (statusMessage == null || statusMessage.getParts() == null) {
            message = null;
        } else {
            message = statusMessage.getParts().stream()
                .map(Part::getText)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        }
    }

    @JsonIgnore
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return updatedAt == null ? null : Instant.parse(updatedAt).toString();
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        updatedAt = timestamp;
    }

    @JsonIgnore
    public Map<String, Object> getDetails() {
        return details;
    }

    @JsonIgnore
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
