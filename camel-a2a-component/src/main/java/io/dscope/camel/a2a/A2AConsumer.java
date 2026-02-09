package io.dscope.camel.a2a;

import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import io.dscope.camel.a2a.service.A2AConsumerRouteService;

/**
 * Consumer for receiving messages from A2A WebSocket endpoints.
 * Sets up a WebSocket route that processes incoming messages and echoes them back.
 */
public class A2AConsumer extends DefaultConsumer {

    /** The endpoint this consumer belongs to */
    private final A2AEndpoint ep;
    private final A2AConsumerRouteService routeService;

    /**
     * Creates a new A2A consumer.
     *
     * @param ep the parent endpoint
     * @param p the message processor
     */
    public A2AConsumer(A2AEndpoint ep, Processor p) {
        super(ep, p);
        this.ep = ep;
        this.routeService = new A2AConsumerRouteService();
    }

    /**
     * Starts the consumer by adding a WebSocket route to the Camel context.
     * The route receives messages from the configured server URL, processes them,
     * and sends responses back to the same WebSocket.
     *
     * @throws Exception if startup fails
     */
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        routeService.registerRoute(ep, getProcessor());
    }
}
