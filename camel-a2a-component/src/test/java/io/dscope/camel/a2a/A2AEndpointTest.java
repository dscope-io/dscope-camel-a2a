package io.dscope.camel.a2a;

import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AEndpointTest {

    @Test
    void endpointCreatesProducerAndConsumerAndIsSingleton() {
        var context = new DefaultCamelContext();
        var component = new A2AComponent();
        component.setCamelContext(context);

        A2AConfiguration cfg = new A2AConfiguration();
        cfg.setServerUrl("direct:a2a");
        A2AEndpoint endpoint = new A2AEndpoint("a2a://agent", component, cfg, "agent");

        assertSame(cfg, endpoint.getConfiguration());
        assertTrue(endpoint.isSingleton());
        assertTrue(endpoint.createProducer() instanceof A2AProducer);
        assertTrue(endpoint.createConsumer(exchange -> {}) instanceof A2AConsumer);
    }
}
