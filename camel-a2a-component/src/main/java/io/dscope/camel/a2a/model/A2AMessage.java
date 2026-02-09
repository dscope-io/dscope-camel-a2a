package io.dscope.camel.a2a.model;

import java.util.UUID;
import io.dscope.camel.a2a.config.A2AProtocolDefaults;

/**
 * Represents a JSON-RPC 2.0 message.
 */
public class A2AMessage {

    private String jsonrpc = A2AProtocolDefaults.JSONRPC_VERSION;
    private String method;
    private Object params;
    private String id;

    public static A2AMessage request(String method, Object params) {
        A2AMessage m = new A2AMessage();
        m.method = method;
        m.params = params;
        m.id = UUID.randomUUID().toString();
        return m;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String v) {
        jsonrpc = v;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String v) {
        method = v;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object v) {
        params = v;
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }
}
