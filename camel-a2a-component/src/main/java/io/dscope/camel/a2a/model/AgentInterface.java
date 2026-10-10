package io.dscope.camel.a2a.model;

/**
 * An advertised protocol interface for an agent.
 */
public class AgentInterface {

    private String url;
    private String protocolBinding;
    private String protocolVersion;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProtocolBinding() {
        return protocolBinding;
    }

    public void setProtocolBinding(String protocolBinding) {
        this.protocolBinding = protocolBinding;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
