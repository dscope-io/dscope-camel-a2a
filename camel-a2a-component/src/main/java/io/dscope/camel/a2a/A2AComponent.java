package io.dscope.camel.a2a;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import java.util.Map;

/**
 * Camel component for A2A (Agent-to-Agent) communication.
 * Provides endpoints for WebSocket-based A2A message exchange.
 */
public class A2AComponent extends DefaultComponent {

    /**
     * Creates a new A2A endpoint with the given URI and parameters.
     *
     * @param uri the endpoint URI
     * @param remaining the remaining part of the URI after the component prefix
     * @param params the URI parameters
     * @return a new A2AEndpoint instance
     * @throws Exception if endpoint creation fails
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> params) throws Exception {
        A2AConfiguration cfg = new A2AConfiguration();
        setProperties(cfg, params);
        return new A2AEndpoint(uri, this, cfg, remaining);
    }
}
