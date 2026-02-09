package io.dscope.camel.a2a.config;

import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

/**
 * Configuration class for A2A component endpoints.
 */
@UriParams
public class A2AConfiguration {

    @UriPath
    private String agent;

    @UriParam
    private String remoteUrl = "ws://localhost:8081/a2a";

    @UriParam
    private String serverUrl = "ws://0.0.0.0:8081/a2a";

    @UriParam
    private String protocolVersion = "a2a/2025-06-18";

    @UriParam
    private boolean sendToAll;

    @UriParam
    private String authToken;

    @UriParam
    private int retryCount = 3;

    @UriParam
    private long retryDelayMs = 500;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String a) {
        agent = a;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String v) {
        remoteUrl = v;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String v) {
        serverUrl = v;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String v) {
        protocolVersion = v;
    }

    public boolean isSendToAll() {
        return sendToAll;
    }

    public void setSendToAll(boolean v) {
        sendToAll = v;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String v) {
        authToken = v;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int v) {
        retryCount = v;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long v) {
        retryDelayMs = v;
    }
}
