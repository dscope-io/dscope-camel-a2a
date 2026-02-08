package io.dscope.camel.a2a;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AConfigurationTest {

    @Test
    void defaultsArePopulated() {
        A2AConfiguration cfg = new A2AConfiguration();

        assertNull(cfg.getAgent());
        assertEquals("ws://localhost:8081/a2a", cfg.getRemoteUrl());
        assertEquals("ws://0.0.0.0:8081/a2a", cfg.getServerUrl());
        assertEquals("a2a/2025-06-18", cfg.getProtocolVersion());
        assertFalse(cfg.isSendToAll());
        assertNull(cfg.getAuthToken());
        assertEquals(3, cfg.getRetryCount());
        assertEquals(500, cfg.getRetryDelayMs());
    }

    @Test
    void settersUpdateValues() {
        A2AConfiguration cfg = new A2AConfiguration();

        cfg.setAgent("assistant");
        cfg.setRemoteUrl("ws://remote");
        cfg.setServerUrl("direct:a2a");
        cfg.setProtocolVersion("custom/version");
        cfg.setSendToAll(true);
        cfg.setAuthToken("secret");
        cfg.setRetryCount(5);
        cfg.setRetryDelayMs(750);

        assertEquals("assistant", cfg.getAgent());
        assertEquals("ws://remote", cfg.getRemoteUrl());
        assertEquals("direct:a2a", cfg.getServerUrl());
        assertEquals("custom/version", cfg.getProtocolVersion());
        assertTrue(cfg.isSendToAll());
        assertEquals("secret", cfg.getAuthToken());
        assertEquals(5, cfg.getRetryCount());
        assertEquals(750, cfg.getRetryDelayMs());
    }
}
