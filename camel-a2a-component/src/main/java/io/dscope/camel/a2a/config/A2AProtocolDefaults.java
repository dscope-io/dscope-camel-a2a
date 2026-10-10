package io.dscope.camel.a2a.config;

/**
 * Defaults used by the current compatibility layer.
 */
public final class A2AProtocolDefaults {

    public static final String JSONRPC_VERSION = "2.0";
    public static final String DEFAULT_REQUEST_METHOD = "SendMessage";
    public static final String PROTOCOL_VERSION = "1.0";
    public static final String JSON_CONTENT_TYPE = "application/a2a+json";

    private A2AProtocolDefaults() {
    }
}
