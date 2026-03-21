package io.dscope.camel.a2a.catalog;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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

    @Test
    void signatureVerificationFailureIsLogged() {
        AgentCardSigner signer = canonicalJson -> "sig-123";
        AgentCardSignatureVerifier verifier = (canonicalJson, signature) -> false;
        DefaultAgentCardCatalog catalog = new DefaultAgentCardCatalog(
            "agent-log",
            "Agent Log",
            "Extended card",
            "http://localhost:8081/a2a/rpc",
            signer,
            verifier,
            new AllowAllAgentCardPolicyChecker()
        );

        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            var card = catalog.getDiscoveryCard();
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> catalog.getCardSignature(card));
            assertEquals("Agent card signature verification failed", error.getMessage());
        } finally {
            System.setErr(originalErr);
        }

        String logOutput = captured.toString(StandardCharsets.UTF_8);
        assertTrue(logOutput.contains("Signature verification failed for agentId=agent-log"));
    }
}
