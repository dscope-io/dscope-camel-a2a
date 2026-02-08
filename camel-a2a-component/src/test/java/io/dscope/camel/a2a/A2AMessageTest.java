package io.dscope.camel.a2a;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2AMessageTest {

    @Test
    void requestPopulatesFields() {
        Map<String, Object> params = Map.of("key", "value");

        A2AMessage message = A2AMessage.request("tool/run", params);

        assertEquals("2.0", message.getJsonrpc());
        assertEquals("tool/run", message.getMethod());
        assertEquals(params, message.getParams());
        assertNotNull(message.getId());
        assertFalse(message.getId().isBlank());
    }
}
