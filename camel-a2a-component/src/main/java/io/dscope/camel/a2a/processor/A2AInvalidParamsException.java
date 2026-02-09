package io.dscope.camel.a2a.processor;

/**
 * Indicates method parameters do not match expected contract.
 */
public class A2AInvalidParamsException extends RuntimeException {

    public A2AInvalidParamsException(String message) {
        super(message);
    }
}
