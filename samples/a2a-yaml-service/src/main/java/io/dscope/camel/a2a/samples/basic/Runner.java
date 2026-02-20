package io.dscope.camel.a2a.samples.basic;

import io.dscope.camel.a2a.A2AComponentApplicationSupport;
import io.dscope.camel.a2a.samples.SamplePersistenceDefaults;
import org.apache.camel.main.Main;

/**
 * Main application runner for the A2A Camel component demo.
 * Starts the Camel Main application with YAML route configuration.
 */
public class Runner {

    /**
     * Main entry point for the application.
     * Configures Camel to load routes from YAML files and starts the runtime.
     *
     * @param args command line arguments (not used)
     * @throws Exception if startup fails
     */
    public static void main(String[] args) throws Exception {
        SamplePersistenceDefaults.configureRedisPersistence();
        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();
        Main main = support.createMain("basic/routes/*.camel.yaml");
        main.run();
    }
}
