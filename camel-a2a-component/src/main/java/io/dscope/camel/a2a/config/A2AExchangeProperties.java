package io.dscope.camel.a2a.config;

/**
 * Exchange property keys shared across A2A processors.
 */
public final class A2AExchangeProperties {

    public static final String ENVELOPE_TYPE = "a2a.envelope.type";
    public static final String REQUEST_ID = "a2a.request.id";
    public static final String METHOD = "a2a.method";
    public static final String RAW_PAYLOAD = "a2a.raw.payload";
    public static final String NORMALIZED_PARAMS = "a2a.params.normalized";
    public static final String METHOD_RESULT = "a2a.method.result";

    private A2AExchangeProperties() {
    }
}
