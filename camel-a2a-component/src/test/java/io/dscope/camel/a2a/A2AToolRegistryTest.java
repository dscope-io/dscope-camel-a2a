package io.dscope.camel.a2a;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2AToolRegistryTest {

    @Test
    void registerAndListReturnsRegisteredTools() {
        A2AToolRegistry registry = new A2AToolRegistry();
        registry.register("echo", "direct:echo", "Echo values back to caller");
        registry.register("reverse", "direct:reverse", "Reverse supplied text");

        List<Map<String, Object>> tools = registry.list();
        assertEquals(2, tools.size());

        assertTrue(tools.stream().anyMatch(tool ->
            "echo".equals(tool.get("name"))
                && "direct:echo".equals(tool.get("route"))
                && "Echo values back to caller".equals(tool.get("description"))));

        assertTrue(tools.stream().anyMatch(tool ->
            "reverse".equals(tool.get("name"))
                && "direct:reverse".equals(tool.get("route"))
                && "Reverse supplied text".equals(tool.get("description"))));
    }
}
