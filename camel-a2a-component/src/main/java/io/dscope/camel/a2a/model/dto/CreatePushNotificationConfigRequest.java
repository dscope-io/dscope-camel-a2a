package io.dscope.camel.a2a.model.dto;

import java.util.Map;

/**
 * Parameters for CreatePushNotificationConfig.
 */
public class CreatePushNotificationConfigRequest {

    private String taskId;
    private String endpointUrl;
    private String secret;
    private Boolean enabled;
    private Integer maxRetries;
    private Long retryBackoffMs;
    private Map<String, String> headers;
    private Map<String, Object> metadata;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(Long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
