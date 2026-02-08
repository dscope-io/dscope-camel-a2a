package io.dscope.camel.a2a;

import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

/**
 * Configuration class for A2A component endpoints.
 * Defines URI parameters for customizing A2A communication behavior.
 */
@UriParams
public class A2AConfiguration {

    /** The target agent identifier (required URI path parameter) */
    @UriPath
    private String agent;

    /** URL for connecting to remote A2A server */
    @UriParam
    private String remoteUrl = "ws://localhost:8081/a2a";

    /** URL for the local A2A server */
    @UriParam
    private String serverUrl = "ws://0.0.0.0:8081/a2a";

    /** Protocol version for A2A communication */
    @UriParam
    private String protocolVersion = "a2a/2025-06-18";

    /** Whether to send messages to all connected agents */
    @UriParam
    private boolean sendToAll;

    /** Authentication token for secure communication */
    @UriParam
    private String authToken;

    /** Number of retry attempts for failed operations */
    @UriParam
    private int retryCount = 3;

    /** Delay between retry attempts in milliseconds */
    @UriParam
    private long retryDelayMs = 500;

    /**
     * Gets the agent identifier.
     * @return the agent name
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Sets the agent identifier.
     * @param a the agent name
     */
    public void setAgent(String a) {
        agent = a;
    }

    /**
     * Gets the remote server URL.
     * @return the remote URL
     */
    public String getRemoteUrl() {
        return remoteUrl;
    }

    /**
     * Sets the remote server URL.
     * @param v the remote URL
     */
    public void setRemoteUrl(String v) {
        remoteUrl = v;
    }

    /**
     * Gets the local server URL.
     * @return the server URL
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Sets the local server URL.
     * @param v the server URL
     */
    public void setServerUrl(String v) {
        serverUrl = v;
    }

    /**
     * Gets the protocol version.
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the protocol version.
     * @param v the protocol version
     */
    public void setProtocolVersion(String v) {
        protocolVersion = v;
    }

    /**
     * Checks if messages should be sent to all agents.
     * @return true if broadcasting to all agents
     */
    public boolean isSendToAll() {
        return sendToAll;
    }

    /**
     * Sets whether to send messages to all agents.
     * @param v true to broadcast to all agents
     */
    public void setSendToAll(boolean v) {
        sendToAll = v;
    }

    /**
     * Gets the authentication token.
     * @return the auth token
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Sets the authentication token.
     * @param v the auth token
     */
    public void setAuthToken(String v) {
        authToken = v;
    }

    /**
     * Gets the retry count.
     * @return number of retries
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the retry count.
     * @param v number of retries
     */
    public void setRetryCount(int v) {
        retryCount = v;
    }

    /**
     * Gets the retry delay in milliseconds.
     * @return retry delay
     */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Sets the retry delay in milliseconds.
     * @param v retry delay
     */
    public void setRetryDelayMs(long v) {
        retryDelayMs = v;
    }
}
