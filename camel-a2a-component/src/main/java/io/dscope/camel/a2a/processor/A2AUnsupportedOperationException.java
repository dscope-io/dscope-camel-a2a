package io.dscope.camel.a2a.processor;

/**
 * Indicates that a valid protocol operation is unsupported for the current resource state.
 */
public class A2AUnsupportedOperationException extends RuntimeException {

    public A2AUnsupportedOperationException(String message) {
        super(message);
    }
}
