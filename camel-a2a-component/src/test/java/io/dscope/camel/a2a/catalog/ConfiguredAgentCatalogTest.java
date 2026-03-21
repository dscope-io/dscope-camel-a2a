package io.dscope.camel.a2a.catalog;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguredAgentCatalogTest {

    @Test
    void unknownAgentLookupIsLoggedAndRejected() {
        ConfiguredAgentSpec defaultAgent = agent("agent-default", true);
        ConfiguredAgentSpec secondary = agent("agent-secondary", false);
        ConfiguredAgentCatalog catalog = new ConfiguredAgentCatalog(List.of(defaultAgent, secondary));

        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> catalog.requireAgent("missing"));
            assertEquals("Unknown configured agent: missing", error.getMessage());
        } finally {
            System.setErr(originalErr);
        }

        String logOutput = captured.toString(StandardCharsets.UTF_8);
        assertTrue(logOutput.contains("Unknown configured agent requested: missing"));
    }

    private ConfiguredAgentSpec agent(String agentId, boolean defaultAgent) {
        ConfiguredAgentSpec spec = new ConfiguredAgentSpec();
        spec.setAgentId(agentId);
        spec.setName(agentId);
        spec.setDefaultAgent(defaultAgent);
        return spec;
    }
}
