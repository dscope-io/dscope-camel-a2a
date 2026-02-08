package io.dscope.camel.a2a;

import java.util.UUID;

/**
 * Represents a JSON-RPC 2.0 message for A2A (Agent-to-Agent) communication.
 * This class encapsulates the standard JSON-RPC message structure with
 * jsonrpc version, method name, parameters, and unique identifier.
 */
public class A2AMessage {

    /** JSON-RPC protocol version */
    private String jsonrpc = "2.0";

    /** The method name to be invoked */
    private String method;

    /** Parameters for the method call */
    private Object params;

    /** Unique identifier for the request/response */
    private String id;

    /**
     * Creates a new request message with the specified method and parameters.
     * Generates a unique ID for the request.
     *
     * @param method the method name to call
     * @param params the parameters for the method
     * @return a new A2AMessage instance configured as a request
     */
    public static A2AMessage request(String method, Object params) {
        A2AMessage m = new A2AMessage();
        m.method = method;
        m.params = params;
        m.id = UUID.randomUUID().toString();
        return m;
    }

    /**
     * Gets the JSON-RPC version.
     * @return the jsonrpc version string
     */
    public String getJsonrpc() {
        return jsonrpc;
    }

    /**
     * Sets the JSON-RPC version.
     * @param v the jsonrpc version string
     */
    public void setJsonrpc(String v) {
        jsonrpc = v;
    }

    /**
     * Gets the method name.
     * @return the method name
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method name.
     * @param v the method name
     */
    public void setMethod(String v) {
        method = v;
    }

    /**
     * Gets the method parameters.
     * @return the parameters object
     */
    public Object getParams() {
        return params;
    }

    /**
     * Sets the method parameters.
     * @param v the parameters object
     */
    public void setParams(Object v) {
        params = v;
    }

    /**
     * Gets the unique message identifier.
     * @return the message id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique message identifier.
     * @param v the message id
     */
    public void setId(String v) {
        id = v;
    }
}
