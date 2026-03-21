package io.dscope.camel.a2a.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfiguredAgentCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredAgentCatalog.class);

    private final List<ConfiguredAgentSpec> agents;
    private final Map<String, ConfiguredAgentSpec> byId;
    private final ConfiguredAgentSpec defaultAgent;

    public ConfiguredAgentCatalog(List<ConfiguredAgentSpec> agents) {
        if (agents == null || agents.isEmpty()) {
            throw new IllegalArgumentException("Configured agent catalog must contain at least one agent");
        }
        Map<String, ConfiguredAgentSpec> mapped = new LinkedHashMap<>();
        ConfiguredAgentSpec resolvedDefault = null;
        for (ConfiguredAgentSpec agent : agents) {
            if (agent == null) {
                throw new IllegalArgumentException("Configured agent entries must not be null");
            }
            String agentId = required(agent.getAgentId(), "agentId");
            required(agent.getName(), "name");
            if (mapped.putIfAbsent(agentId, agent) != null) {
                throw new IllegalArgumentException("Duplicate configured agentId: " + agentId);
            }
            if (agent.isDefaultAgent()) {
                if (resolvedDefault != null) {
                    throw new IllegalArgumentException("Exactly one configured agent must be marked default");
                }
                resolvedDefault = agent;
            }
        }
        if (resolvedDefault == null) {
            throw new IllegalArgumentException("One configured agent must be marked default");
        }
        this.agents = List.copyOf(agents);
        this.byId = Map.copyOf(mapped);
        this.defaultAgent = resolvedDefault;
    }

    public List<ConfiguredAgentSpec> agents() {
        return agents;
    }

    public ConfiguredAgentSpec defaultAgent() {
        return defaultAgent;
    }

    public ConfiguredAgentSpec requireAgent(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            LOG.debug("No agentId provided; falling back to default agent {}", defaultAgent.getAgentId());
            return defaultAgent;
        }
        String trimmedAgentId = agentId.trim();
        ConfiguredAgentSpec resolved = byId.get(trimmedAgentId);
        if (resolved == null) {
            LOG.error("Unknown configured agent requested: {}", trimmedAgentId);
            throw new IllegalArgumentException("Unknown configured agent: " + agentId);
        }
        LOG.debug("Resolved configured agent {} to {}", trimmedAgentId, resolved.getName());
        return resolved;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Configured agent " + field + " is required");
        }
        return value.trim();
    }
}
