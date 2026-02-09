package io.dscope.camel.a2a.catalog;

import org.apache.camel.BindToRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for A2A tool definitions.
 */
@BindToRegistry("a2aToolRegistry")
public class A2AToolRegistry {

    public record ToolDef(String name, String route, String description) {
    }

    private final Map<String, ToolDef> tools = new ConcurrentHashMap<>();

    public void register(String name, String route, String desc) {
        tools.put(name, new ToolDef(name, route, desc));
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var t : tools.values()) {
            out.add(Map.of(
                "name", t.name(),
                "route", t.route(),
                "description", t.description()
            ));
        }
        return out;
    }
}
