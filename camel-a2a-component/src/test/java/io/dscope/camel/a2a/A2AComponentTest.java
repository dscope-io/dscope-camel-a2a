package io.dscope.camel.a2a;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2AComponentTest {

    @Test
    void createEndpointAppliesConfigurationParameters() throws Exception {
        var context = new DefaultCamelContext();
        var component = new A2AComponent();
        component.setCamelContext(context);

        Map<String, Object> params = new HashMap<>();
        params.put("remoteUrl", "ws://remote.example/a2a");
        params.put("sendToAll", true);
        params.put("retryCount", 7);

        Endpoint endpoint = component.createEndpoint("a2a://assistant", "assistant", params);
        assertTrue(endpoint instanceof A2AEndpoint);

        A2AConfiguration cfg = ((A2AEndpoint) endpoint).getConfiguration();
        assertEquals("ws://remote.example/a2a", cfg.getRemoteUrl());
        assertTrue(cfg.isSendToAll());
        assertEquals(7, cfg.getRetryCount());
    }
}
