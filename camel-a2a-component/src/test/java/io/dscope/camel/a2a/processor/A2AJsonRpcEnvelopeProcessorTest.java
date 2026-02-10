package io.dscope.camel.a2a.processor;

import java.util.Map;
import java.util.Set;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import io.dscope.camel.a2a.config.A2AExchangeProperties;

import static org.junit.jupiter.api.Assertions.*;

class A2AJsonRpcEnvelopeProcessorTest {

    @Test
    void processRejectsMalformedJson() {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody("{not valid json");

        var processor = new A2AJsonRpcEnvelopeProcessor();

        var error = assertThrows(A2AJsonRpcValidationException.class, () -> processor.process(exchange));
        assertEquals("Malformed JSON-RPC payload", error.getMessage());
    }

    @Test
    void processRejectsMissingJsonRpcVersion() {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody(Map.of("method", "intent/execute", "params", Map.of("x", 1), "id", "1"));

        var processor = new A2AJsonRpcEnvelopeProcessor();

        var error = assertThrows(A2AJsonRpcValidationException.class, () -> processor.process(exchange));
        assertEquals("Invalid JSON-RPC envelope: jsonrpc must be \"2.0\"", error.getMessage());
    }

    @Test
    void processNormalizesRequestAndSetsProperties() throws Exception {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody("""
            {"jsonrpc":"2.0","method":"intent/execute","params":{"foo":"bar"},"id":"42"}
            """);

        var processor = new A2AJsonRpcEnvelopeProcessor(Set.of("intent/execute"));
        processor.process(exchange);

        assertEquals("request", exchange.getProperty(A2AExchangeProperties.ENVELOPE_TYPE));
        assertEquals("42", exchange.getProperty(A2AExchangeProperties.REQUEST_ID));
        assertEquals("intent/execute", exchange.getProperty(A2AExchangeProperties.METHOD));
        assertTrue(exchange.getProperty(A2AExchangeProperties.RAW_PAYLOAD, String.class).contains("\"jsonrpc\":\"2.0\""));

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        assertEquals("bar", params.get("foo"));
    }

    @Test
    void processRejectsUnknownMethodWhenWhitelistEnabled() {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody("""
            {"jsonrpc":"2.0","method":"GetTask","params":{},"id":"7"}
            """);

        var processor = new A2AJsonRpcEnvelopeProcessor(Set.of("intent/execute"));

        var error = assertThrows(A2AMethodNotFoundException.class, () -> processor.process(exchange));
        assertEquals("Method not found: GetTask", error.getMessage());
    }

    @Test
    void processAcceptsBinaryJsonPayload() throws Exception {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody("""
            {"jsonrpc":"2.0","method":"intent/execute","params":{"foo":"bar"},"id":"99"}
            """.getBytes());

        var processor = new A2AJsonRpcEnvelopeProcessor(Set.of("intent/execute"));
        processor.process(exchange);

        assertEquals("request", exchange.getProperty(A2AExchangeProperties.ENVELOPE_TYPE));
        assertEquals("99", exchange.getProperty(A2AExchangeProperties.REQUEST_ID));
        assertEquals("intent/execute", exchange.getProperty(A2AExchangeProperties.METHOD));
    }
}
