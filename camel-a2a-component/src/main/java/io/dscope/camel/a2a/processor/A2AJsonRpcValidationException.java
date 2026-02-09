package io.dscope.camel.a2a.processor;

/**
 * Indicates a JSON-RPC envelope validation failure.
 */
public class A2AJsonRpcValidationException extends RuntimeException {

    public A2AJsonRpcValidationException(String message) {
        super(message);
    }

    public A2AJsonRpcValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
