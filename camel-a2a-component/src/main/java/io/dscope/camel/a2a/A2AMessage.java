package io.dscope.camel.a2a;

import java.util.UUID;

/**
 * Backward-compatible type alias for the A2A message model.
 */
@Deprecated(forRemoval = false)
public class A2AMessage extends io.dscope.camel.a2a.model.A2AMessage {

    /**
     * Creates a root-package request instance for compatibility.
     *
     * @param method the method name to call
     * @param params the parameters for the method
     * @return a new A2AMessage instance configured as a request
     */
    public static A2AMessage request(String method, Object params) {
        A2AMessage m = new A2AMessage();
        m.setMethod(method);
        m.setParams(params);
        m.setId(UUID.randomUUID().toString());
        return m;
    }
}
