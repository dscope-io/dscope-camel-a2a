package io.dscope.camel.a2a;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcErrorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void envelopeIncludesIdWhenProvided() throws Exception {
        String json = JsonRpcError.envelope("42", -32601, "No such method");

        JsonNode node = mapper.readTree(json);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals(-32601, node.get("error").get("code").asInt());
        assertEquals("No such method", node.get("error").get("message").asText());
        assertEquals("42", node.get("id").asText());
    }

    @Test
    void envelopeOmitsIdWhenNotProvided() throws Exception {
        String json = JsonRpcError.envelope(null, 500, "Internal error");

        JsonNode node = mapper.readTree(json);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals(500, node.get("error").get("code").asInt());
        assertEquals("Internal error", node.get("error").get("message").asText());
        assertFalse(node.has("id"));
    }
}
