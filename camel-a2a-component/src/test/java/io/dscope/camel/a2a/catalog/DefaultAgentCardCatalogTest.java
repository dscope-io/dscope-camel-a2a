package io.dscope.camel.a2a.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAgentCardCatalogTest {

    @Test
    void discoveryCardContainsCapabilitiesAndSecuritySchemes() {
        DefaultAgentCardCatalog catalog = new DefaultAgentCardCatalog(
            "agent-1",
            "Agent One",
            "Discovery card",
            "http://localhost:8081/a2a/rpc",
            new NoopAgentCardSigner(),
            new AllowAllAgentCardSignatureVerifier(),
            new AllowAllAgentCardPolicyChecker()
        );

        var card = catalog.getDiscoveryCard();
        assertEquals("agent-1", card.getAgentId());
        assertNotNull(card.getCapabilities());
        assertTrue(card.getCapabilities().isStreaming());
        assertNotNull(card.getSecuritySchemes());
        assertTrue(card.getSecuritySchemes().containsKey("bearerAuth"));
    }

    @Test
    void extendedCardCanIncludeSignature() {
        AgentCardSigner signer = canonicalJson -> "sig-123";
        DefaultAgentCardCatalog catalog = new DefaultAgentCardCatalog(
            "agent-2",
            "Agent Two",
            "Extended card",
            "http://localhost:8081/a2a/rpc",
            signer,
            new AllowAllAgentCardSignatureVerifier(),
            new AllowAllAgentCardPolicyChecker()
        );

        var card = catalog.getExtendedCard();
        String signature = catalog.getCardSignature(card);
        assertEquals("sig-123", signature);
        assertEquals(Boolean.TRUE, card.getMetadata().get("extended"));
    }
}
