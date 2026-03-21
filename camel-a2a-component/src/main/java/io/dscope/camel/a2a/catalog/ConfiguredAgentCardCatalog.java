package io.dscope.camel.a2a.catalog;

import io.dscope.camel.a2a.model.AgentCard;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguredAgentCardCatalog implements AgentCardCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredAgentCardCatalog.class);

    private final ConfiguredAgentCatalog configuredAgentCatalog;
    private final String endpointUrl;
    private final AgentCardSigner signer;
    private final AgentCardSignatureVerifier verifier;
    private final AgentCardPolicyChecker policyChecker;

    public ConfiguredAgentCardCatalog(ConfiguredAgentCatalog configuredAgentCatalog, String endpointUrl) {
        this(
            configuredAgentCatalog,
            endpointUrl,
            new NoopAgentCardSigner(),
            new AllowAllAgentCardSignatureVerifier(),
            new AllowAllAgentCardPolicyChecker()
        );
    }

    public ConfiguredAgentCardCatalog(ConfiguredAgentCatalog configuredAgentCatalog,
                                      String endpointUrl,
                                      AgentCardSigner signer,
                                      AgentCardSignatureVerifier verifier,
                                      AgentCardPolicyChecker policyChecker) {
        if (configuredAgentCatalog == null) {
            throw new IllegalArgumentException("configuredAgentCatalog is required");
        }
        this.configuredAgentCatalog = configuredAgentCatalog;
        this.endpointUrl = endpointUrl;
        this.signer = signer == null ? new NoopAgentCardSigner() : signer;
        this.verifier = verifier == null ? new AllowAllAgentCardSignatureVerifier() : verifier;
        this.policyChecker = policyChecker == null ? new AllowAllAgentCardPolicyChecker() : policyChecker;
        LOG.debug("ConfiguredAgentCardCatalog initialized with {} configured agents, defaultAgent={}, endpointUrl={}",
            configuredAgentCatalog.agents().size(),
            configuredAgentCatalog.defaultAgent().getAgentId(),
            endpointUrl);
    }

    @Override
    public AgentCard getDiscoveryCard() {
        LOG.debug("Building discovery card from configured agent catalog");
        try {
            return enrich(baseCatalog().getDiscoveryCard(), false);
        } catch (RuntimeException e) {
            LOG.error("Failed to build discovery card", e);
            throw e;
        }
    }

    @Override
    public AgentCard getExtendedCard() {
        LOG.debug("Building extended discovery card from configured agent catalog");
        try {
            return enrich(baseCatalog().getExtendedCard(), true);
        } catch (RuntimeException e) {
            LOG.error("Failed to build extended card", e);
            throw e;
        }
    }

    @Override
    public String getCardSignature(AgentCard card) {
        String cardId = card == null ? "<null>" : card.getAgentId();
        LOG.debug("Signing configured agent card for agentId={}", cardId);
        try {
            return baseCatalog().getCardSignature(card);
        } catch (RuntimeException e) {
            LOG.error("Failed to sign configured agent card for agentId={}", cardId, e);
            throw e;
        }
    }

    protected Map<String, Object> additionalAgentMetadata(ConfiguredAgentSpec spec) {
        return Map.of();
    }

    protected void enrichCardMetadata(Map<String, Object> metadata, boolean extended) {
    }

    private DefaultAgentCardCatalog baseCatalog() {
        ConfiguredAgentSpec defaultAgent = configuredAgentCatalog.defaultAgent();
        return new DefaultAgentCardCatalog(
            defaultAgent.getAgentId(),
            defaultAgent.getName(),
            defaultAgent.getDescription(),
            endpointUrl,
            signer,
            verifier,
            policyChecker
        );
    }

    private AgentCard enrich(AgentCard card, boolean extended) {
        ConfiguredAgentSpec defaultAgent = configuredAgentCatalog.defaultAgent();
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (card.getMetadata() != null && !card.getMetadata().isEmpty()) {
            metadata.putAll(card.getMetadata());
        }
        metadata.put("discovery", true);
        metadata.put("extended", extended);
        metadata.put("defaultAgentId", defaultAgent.getAgentId());
        metadata.put("agents", configuredAgentCatalog.agents().stream().map(this::agentMetadata).toList());
        enrichCardMetadata(metadata, extended);
        card.setVersion(defaultVersion(defaultAgent));
        card.setMetadata(metadata);
        LOG.debug("Enriched {} card for defaultAgentId={}, metadataKeys={}",
            extended ? "extended" : "discovery",
            defaultAgent.getAgentId(),
            metadata.keySet());
        return card;
    }

    private Map<String, Object> agentMetadata(ConfiguredAgentSpec spec) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("agentId", spec.getAgentId());
        data.put("name", spec.getName());
        data.put("description", spec.getDescription() == null ? "" : spec.getDescription());
        data.put("version", defaultVersion(spec));
        data.put("default", spec.isDefaultAgent());
        data.put("skills", spec.getSkills());
        if (!spec.getMetadata().isEmpty()) {
            data.put("metadata", spec.getMetadata());
        }
        Map<String, Object> additional = additionalAgentMetadata(spec);
        if (additional != null && !additional.isEmpty()) {
            data.putAll(additional);
        }
        return data;
    }

    private String defaultVersion(ConfiguredAgentSpec spec) {
        if (spec.getVersion() != null && !spec.getVersion().isBlank()) {
            return spec.getVersion();
        }
        LOG.debug("Configured agent {} has no version; using default 1.0.0", spec.getAgentId());
        return "1.0.0";
    }
}
