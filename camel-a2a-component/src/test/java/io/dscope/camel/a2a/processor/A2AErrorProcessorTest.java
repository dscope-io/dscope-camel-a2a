package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import io.dscope.camel.a2a.config.A2AExchangeProperties;

import static org.junit.jupiter.api.Assertions.*;

class A2AErrorProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void processMapsMethodNotFoundAndEchoesRequestId() throws Exception {
        var exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(A2AExchangeProperties.REQUEST_ID, "req-1");
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new A2AMethodNotFoundException("Method not found: Foo"));

        new A2AErrorProcessor().process(exchange);

        JsonNode node = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals(-32601, node.get("error").get("code").asInt());
        assertEquals("Method not found: Foo", node.get("error").get("message").asText());
        assertEquals("req-1", node.get("id").asText());
    }

    @Test
    void processMapsInvalidParams() throws Exception {
        var exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(A2AExchangeProperties.REQUEST_ID, 9);
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new A2AInvalidParamsException("missing field"));

        new A2AErrorProcessor().process(exchange);

        JsonNode node = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals(-32602, node.get("error").get("code").asInt());
        assertEquals(9, node.get("id").asInt());
    }

    @Test
    void processUsesPayloadIdForInvalidRequest() throws Exception {
        var exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(A2AExchangeProperties.RAW_PAYLOAD, """
            {"jsonrpc":"2.0","method":17,"id":"abc"}
            """);
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT,
            new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: method must be a non-empty string"));

        new A2AErrorProcessor().process(exchange);

        JsonNode node = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals(-32600, node.get("error").get("code").asInt());
        assertEquals("abc", node.get("id").asText());
    }

    @Test
    void processMapsInternalErrorAndIncludesNullId() throws Exception {
        var exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new RuntimeException("boom"));

        new A2AErrorProcessor().process(exchange);

        JsonNode node = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals(-32603, node.get("error").get("code").asInt());
        assertEquals("boom", node.get("error").get("message").asText());
        assertTrue(node.has("id"));
        assertTrue(node.get("id").isNull());
        assertEquals("application/json", exchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
    }
}
