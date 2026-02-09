package io.dscope.camel.a2a.model;

import java.util.List;

/**
 * Security scheme declaration for an agent card.
 */
public class AgentSecurityScheme {

    private String type;
    private String scheme;
    private String description;
    private List<String> scopes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
