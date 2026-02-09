package io.dscope.camel.a2a.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import io.dscope.camel.a2a.model.AgentCapabilities;
import io.dscope.camel.a2a.model.AgentCard;
import io.dscope.camel.a2a.model.AgentSecurityScheme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Default catalog implementation for discovery and extended cards.
 */
public class DefaultAgentCardCatalog implements AgentCardCatalog {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String agentId;
    private final String name;
    private final String description;
    private final String endpointUrl;
    private final AgentCardSigner signer;
    private final AgentCardSignatureVerifier verifier;
    private final AgentCardPolicyChecker policyChecker;

    public DefaultAgentCardCatalog(String agentId,
                                   String name,
                                   String description,
                                   String endpointUrl,
                                   AgentCardSigner signer,
                                   AgentCardSignatureVerifier verifier,
                                   AgentCardPolicyChecker policyChecker) {
        this.agentId = agentId;
        this.name = name;
        this.description = description;
        this.endpointUrl = endpointUrl;
        this.signer = signer == null ? new NoopAgentCardSigner() : signer;
        this.verifier = verifier == null ? new AllowAllAgentCardSignatureVerifier() : verifier;
        this.policyChecker = policyChecker == null ? new AllowAllAgentCardPolicyChecker() : policyChecker;
    }

    @Override
    public AgentCard getDiscoveryCard() {
        AgentCard card = baseCard();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("discovery", true);
        card.setMetadata(metadata);
        applyPolicy(card);
        return card;
    }

    @Override
    public AgentCard getExtendedCard() {
        AgentCard card = baseCard();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("discovery", true);
        metadata.put("extended", true);
        metadata.put("securityHooks", Map.of(
            "signing", true,
            "verification", true,
            "policyChecks", true
        ));
        String signature = getCardSignature(card);
        if (signature != null) {
            metadata.put("jws", signature);
        }
        card.setMetadata(metadata);
        applyPolicy(card);
        return card;
    }

    @Override
    public String getCardSignature(AgentCard card) {
        try {
            String canonical = mapper.writeValueAsString(card);
            String signature = signer.sign(canonical);
            if (signature != null && !verifier.verify(canonical, signature)) {
                throw new IllegalStateException("Agent card signature verification failed");
            }
            return signature;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign agent card", e);
        }
    }

    private void applyPolicy(AgentCard card) {
        policyChecker.validate(card);
    }

    private AgentCard baseCard() {
        AgentCapabilities capabilities = new AgentCapabilities();
        capabilities.setStreaming(true);
        capabilities.setPushNotifications(true);
        capabilities.setStatefulTasks(true);
        capabilities.setSupportedMethods(List.copyOf(new TreeSet<>(A2AProtocolMethods.CORE_METHODS)));

        AgentSecurityScheme bearer = new AgentSecurityScheme();
        bearer.setType("http");
        bearer.setScheme("bearer");
        bearer.setDescription("Bearer token authentication");
        bearer.setScopes(List.of("a2a.read", "a2a.write"));

        AgentCard card = new AgentCard();
        card.setAgentId(agentId);
        card.setName(name);
        card.setDescription(description);
        card.setEndpointUrl(endpointUrl);
        card.setVersion("0.5.0");
        card.setCapabilities(capabilities);
        card.setSecuritySchemes(Map.of("bearerAuth", bearer));
        card.setDefaultInputModes(List.of("application/json", "text/plain"));
        card.setDefaultOutputModes(List.of("application/json", "text/event-stream"));
        return card;
    }
}
