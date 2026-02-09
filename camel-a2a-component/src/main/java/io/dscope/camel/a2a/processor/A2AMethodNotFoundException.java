package io.dscope.camel.a2a.processor;

/**
 * Indicates the requested JSON-RPC method is not supported.
 */
public class A2AMethodNotFoundException extends RuntimeException {

    public A2AMethodNotFoundException(String message) {
        super(message);
    }
}
