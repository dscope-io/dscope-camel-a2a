package io.dscope.camel.a2a;

import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AConsumerTest {

    @Test
    void doStartRegistersEchoRouteOnCamelContext() throws Exception {
        var context = new DefaultCamelContext();
        var component = new A2AComponent();
        component.setCamelContext(context);

        A2AEndpoint endpoint = (A2AEndpoint) component.createEndpoint("a2a://agent", "agent", java.util.Map.of());
        endpoint.getConfiguration().setServerUrl("direct:a2a");

        var consumer = new A2AConsumer(endpoint, exchange -> {});

        context.start();
        try {
            consumer.start();
            assertEquals(1, context.getRoutes().size());
            assertEquals("direct://a2a", context.getRoutes().get(0).getEndpoint().getEndpointUri());
        } finally {
            consumer.stop();
            context.stop();
        }
    }
}
