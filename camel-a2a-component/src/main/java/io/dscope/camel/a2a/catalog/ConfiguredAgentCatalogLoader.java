package io.dscope.camel.a2a.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfiguredAgentCatalogLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public ConfiguredAgentCatalog load(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Configured agent catalog location is required");
        }
        try (InputStream stream = open(location.trim())) {
            if (stream == null) {
                throw new IllegalArgumentException("Configured agent catalog not found: " + location);
            }
            Root root = YAML_MAPPER.readValue(stream, Root.class);
            List<ConfiguredAgentSpec> agents = root == null
                ? List.of()
                : root.agents != null && !root.agents.isEmpty()
                    ? root.agents
                    : root.configuredAgents;
            return new ConfiguredAgentCatalog(agents == null ? List.of() : agents);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load configured agent catalog: " + location, e);
        }
    }

    private InputStream open(String location) throws Exception {
        if (location.startsWith("classpath:")) {
            return getClass().getClassLoader().getResourceAsStream(location.substring("classpath:".length()));
        }
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return new URL(location).openStream();
        }
        if (location.startsWith("file:")) {
            return Files.newInputStream(Path.of(URI.create(location)));
        }
        return Files.newInputStream(Path.of(location));
    }

    private static final class Root {
        public List<ConfiguredAgentSpec> agents;
        public List<ConfiguredAgentSpec> configuredAgents;
    }
}
