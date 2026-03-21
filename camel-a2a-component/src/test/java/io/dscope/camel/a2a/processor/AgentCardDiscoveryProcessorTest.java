package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.catalog.AgentCardCatalog;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardPolicyChecker;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardSignatureVerifier;
import io.dscope.camel.a2a.catalog.DefaultAgentCardCatalog;
import io.dscope.camel.a2a.catalog.NoopAgentCardSigner;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AgentCardDiscoveryProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void processReturnsDiscoveryCardJson() throws Exception {
        DefaultAgentCardCatalog catalog = new DefaultAgentCardCatalog(
            "agent-discovery",
            "Discovery Agent",
            "Description",
            "http://localhost:8081/a2a/rpc",
            new NoopAgentCardSigner(),
            new AllowAllAgentCardSignatureVerifier(),
            new AllowAllAgentCardPolicyChecker()
        );
        AgentCardDiscoveryProcessor processor = new AgentCardDiscoveryProcessor(catalog);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        processor.process(exchange);

        JsonNode card = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals("agent-discovery", card.get("agentId").asText());
        assertTrue(card.get("capabilities").get("streaming").asBoolean());
        assertEquals("application/json", exchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
    }

    @Test
    void processLogsAndPropagatesWhenCatalogFails() {
        AgentCardCatalog failingCatalog = new AgentCardCatalog() {
            @Override
            public io.dscope.camel.a2a.model.AgentCard getDiscoveryCard() {
                throw new IllegalStateException("boom");
            }

            @Override
            public io.dscope.camel.a2a.model.AgentCard getExtendedCard() {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public String getCardSignature(io.dscope.camel.a2a.model.AgentCard card) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AgentCardDiscoveryProcessor processor = new AgentCardDiscoveryProcessor(failingCatalog);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            assertThrows(IllegalStateException.class, () -> processor.process(exchange));
        } finally {
            System.setErr(originalErr);
        }

        String logOutput = captured.toString(StandardCharsets.UTF_8);
        assertTrue(logOutput.contains("Failed to process discovery card request"));
    }
}
