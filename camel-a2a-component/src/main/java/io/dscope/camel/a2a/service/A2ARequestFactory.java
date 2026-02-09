package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.config.A2AProtocolDefaults;
import io.dscope.camel.a2a.model.A2AMessage;

/**
 * Builds outbound A2A request envelopes.
 */
public class A2ARequestFactory {

    private final String defaultMethod;

    public A2ARequestFactory() {
        this(A2AProtocolDefaults.DEFAULT_REQUEST_METHOD);
    }

    public A2ARequestFactory(String defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    public A2AMessage createRequest(Object params) {
        return A2AMessage.request(defaultMethod, params);
    }
}
