package io.dscope.camel.a2a.service;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import io.dscope.camel.a2a.A2AEndpoint;

/**
 * Registers consumer routes for A2A endpoints.
 */
public class A2AConsumerRouteService {

    public void registerRoute(A2AEndpoint endpoint, Processor processor) throws Exception {
        final String wsUri = endpoint.getConfiguration().getServerUrl();
        final String routeId = buildRouteId(wsUri);

        if (endpoint.getCamelContext().getRoute(routeId) != null) {
            return;
        }

        endpoint.getCamelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(wsUri)
                    .routeId(routeId)
                    .process(processor)
                    .to(wsUri);
            }
        });
    }

    private String buildRouteId(String wsUri) {
        return "a2a-consumer-" + Integer.toHexString(wsUri.hashCode());
    }
}
