package io.dscope.camel.a2a;

import org.apache.camel.Category;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;

/**
 * Camel endpoint for A2A (Agent-to-Agent) communication.
 * Supports both producing messages to A2A servers and consuming messages from them.
 */
@UriEndpoint(
    firstVersion = "0.5.0",
    scheme = "a2a",
    title = "A2A",
    syntax = "a2a:agent",
    remote = true,
    category = {Category.RPC, Category.AI}
)
public class A2AEndpoint extends DefaultEndpoint {

    @UriPath(description = "Target agent identifier")
    private String agent;

    /** Configuration for this endpoint */
    private final A2AConfiguration cfg;

    /** Remaining part of the URI after the component prefix */
    @SuppressWarnings("unused")
    private final String remaining;

    /**
     * Creates a new A2A endpoint.
     *
     * @param uri the endpoint URI
     * @param c the parent component
     * @param cfg the endpoint configuration
     * @param remaining the remaining URI part
     */
    public A2AEndpoint(String uri, Component c, A2AConfiguration cfg, String remaining) {
        super(uri, c);
        this.cfg = cfg;
        this.agent = remaining;
        this.cfg.setAgent(remaining);
        this.remaining = remaining;
    }

    /**
     * Creates a producer for sending messages to A2A servers.
     *
     * @return a new A2AProducer instance
     */
    @Override
    public Producer createProducer() {
        return new A2AProducer(this);
    }

    /**
     * Creates a consumer for receiving messages from A2A servers.
     *
     * @param p the message processor
     * @return a new A2AConsumer instance
     */
    @Override
    public Consumer createConsumer(Processor p) {
        return new A2AConsumer(this, p);
    }

    /**
     * Indicates that this endpoint is a singleton.
     *
     * @return true since endpoints are singletons
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Gets the endpoint configuration.
     *
     * @return the A2A configuration
     */
    public A2AConfiguration getConfiguration() {
        return cfg;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
        this.cfg.setAgent(agent);
    }
}
