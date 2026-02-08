package io.dscope.camel.a2a;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntentRouterProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void processHandlesA2AMessageBodies() throws Exception {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        var message = A2AMessage.request("tools.echo", Map.of("text", "hello"));
        exchange.getIn().setBody(message);

        var processor = new IntentRouterProcessor();
        processor.process(exchange);

        JsonNode response = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertTrue(response.get("handled").asBoolean());
        assertEquals("tools.echo", response.get("method").asText());
    }

    @Test
    void processHandlesJsonStringBodies() throws Exception {
        var context = new DefaultCamelContext();
        var exchange = new DefaultExchange(context);
        var message = A2AMessage.request("tools.info", Map.of("id", 101));
        exchange.getIn().setBody(mapper.writeValueAsString(message));

        var processor = new IntentRouterProcessor();
        processor.process(exchange);

        JsonNode response = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertTrue(response.get("handled").asBoolean());
        assertEquals("tools.info", response.get("method").asText());
    }
}
