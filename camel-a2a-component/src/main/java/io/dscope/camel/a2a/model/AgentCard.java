package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

/**
 * Agent discovery card model.
 */
public class AgentCard {

    private String agentId;
    private String name;
    private String description;
    private String endpointUrl;
    private String version;
    private List<AgentInterface> supportedInterfaces;
    private AgentCapabilities capabilities;
    private Map<String, AgentSecurityScheme> securitySchemes;
    private List<String> defaultInputModes;
    private List<String> defaultOutputModes;
    private Map<String, Object> metadata;

    @JsonIgnore
    public String getAgentId() {
        return agentId;
    }

    @JsonIgnore
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @JsonIgnore
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public List<AgentInterface> getSupportedInterfaces() {
        return supportedInterfaces;
    }

    public void setSupportedInterfaces(List<AgentInterface> supportedInterfaces) {
        this.supportedInterfaces = supportedInterfaces;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AgentCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(AgentCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, AgentSecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }

    public void setSecuritySchemes(Map<String, AgentSecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    public List<String> getDefaultInputModes() {
        return defaultInputModes;
    }

    public void setDefaultInputModes(List<String> defaultInputModes) {
        this.defaultInputModes = defaultInputModes;
    }

    public List<String> getDefaultOutputModes() {
        return defaultOutputModes;
    }

    public void setDefaultOutputModes(List<String> defaultOutputModes) {
        this.defaultOutputModes = defaultOutputModes;
    }

    @JsonIgnore
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @JsonIgnore
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
