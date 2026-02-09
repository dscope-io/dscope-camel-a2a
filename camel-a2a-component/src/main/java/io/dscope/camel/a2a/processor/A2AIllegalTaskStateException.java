package io.dscope.camel.a2a.processor;

/**
 * Raised when a task lifecycle transition is not allowed.
 */
public class A2AIllegalTaskStateException extends A2AInvalidParamsException {

    public A2AIllegalTaskStateException(String message) {
        super(message);
    }
}
