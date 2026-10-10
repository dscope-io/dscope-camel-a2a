package io.dscope.camel.a2a;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2AProducerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void processWrapsBodyInSendMessageRequest() throws Exception {
        var context = new DefaultCamelContext();
        var component = new A2AComponent();
        component.setCamelContext(context);

        A2AEndpoint endpoint = (A2AEndpoint) component.createEndpoint("a2a://agent", "agent", Map.of());
        A2AProducer producer = new A2AProducer(endpoint);

        var exchange = new DefaultExchange(context);
        exchange.getIn().setBody(Map.of("message", Map.of(
            "messageId", "outbound-1",
            "role", "ROLE_USER",
            "parts", java.util.List.of(Map.of("text", "hello"))
        )));

        producer.process(exchange);

        String json = exchange.getMessage().getBody(String.class);
        JsonNode node = mapper.readTree(json);
        assertEquals("SendMessage", node.get("method").asText());
        assertEquals("hello", node.get("params").get("message").get("parts").get(0).get("text").asText());
        assertTrue(node.hasNonNull("id"));
    }
}
