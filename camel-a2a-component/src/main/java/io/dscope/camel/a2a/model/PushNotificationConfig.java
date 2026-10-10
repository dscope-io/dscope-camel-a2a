package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Push notification delivery configuration.
 */
public class PushNotificationConfig {

    private String configId;
    private String taskId;
    private String token;
    private Map<String, Object> authentication;
    private String endpointUrl;
    private String secret;
    private boolean enabled = true;
    private int maxRetries = 3;
    private long retryBackoffMs = 100;
    private Map<String, String> headers;
    private Map<String, Object> metadata;
    private String createdAt;
    private String updatedAt;

    @JsonProperty("id")
    public String getConfigId() {
        return configId;
    }

    @JsonProperty("id")
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Map<String, Object> authentication) {
        this.authentication = authentication;
    }

    @JsonProperty("url")
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @JsonProperty("url")
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @JsonIgnore
    public String getSecret() {
        return secret;
    }

    @JsonIgnore
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public int getMaxRetries() {
        return maxRetries;
    }

    @JsonIgnore
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @JsonIgnore
    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    @JsonIgnore
    public void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    @JsonIgnore
    public Map<String, String> getHeaders() {
        return headers;
    }

    @JsonIgnore
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @JsonIgnore
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @JsonIgnore
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
