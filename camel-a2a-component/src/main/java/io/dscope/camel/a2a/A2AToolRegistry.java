package io.dscope.camel.a2a;

import org.apache.camel.BindToRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for A2A tools that can be invoked via intent messages.
 * Maintains a thread-safe collection of tool definitions with their routes and descriptions.
 */
@BindToRegistry("a2aToolRegistry")
public class A2AToolRegistry {

    /**
     * Record representing a tool definition.
     */
    public record ToolDef(String name, String route, String description) {}

    /** Thread-safe map of registered tools */
    private final Map<String, ToolDef> tools = new ConcurrentHashMap<>();

    /**
     * Registers a new tool in the registry.
     *
     * @param name the tool name
     * @param route the Camel route URI for the tool
     * @param desc the tool description
     */
    public void register(String name, String route, String desc) {
        tools.put(name, new ToolDef(name, route, desc));
    }

    /**
     * Lists all registered tools as a list of maps.
     * Each map contains name, route, and description for a tool.
     *
     * @return list of tool definitions as maps
     */
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
