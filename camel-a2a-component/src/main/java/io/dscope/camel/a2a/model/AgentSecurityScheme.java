package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Security scheme declaration for an agent card.
 */
public class AgentSecurityScheme {

    private String type;
    private String scheme;
    private String description;
    private List<String> scopes;

    @JsonIgnore
    public String getType() {
        return type;
    }

    @JsonIgnore
    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public String getScheme() {
        return scheme;
    }

    @JsonIgnore
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @JsonProperty("httpAuthSecurityScheme")
    public Map<String, String> getHttpAuthSecurityScheme() {
        return Map.of("scheme", scheme == null ? "Bearer" : scheme, "description", description == null ? "" : description);
    }

    @JsonProperty("httpAuthSecurityScheme")
    public void setHttpAuthSecurityScheme(Map<String, String> httpAuthSecurityScheme) {
        if (httpAuthSecurityScheme != null) {
            scheme = httpAuthSecurityScheme.get("scheme");
            description = httpAuthSecurityScheme.get("description");
        }
    }

    @JsonIgnore
    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public List<String> getScopes() {
        return scopes;
    }

    @JsonIgnore
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
