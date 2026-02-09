package io.dscope.camel.a2a.samples.standalone;

import io.dscope.camel.a2a.A2AComponentApplicationSupport;
import org.apache.camel.main.Main;

/**
 * Standalone sample runner for the A2A Camel component.
 * This sample demonstrates a minimal setup with custom configuration.
 */
public class Runner {

    /**
     * Main entry point for the standalone sample.
     * Configures Camel to load routes from the standalone sample directory.
     *
     * @param args command line arguments (not used)
     * @throws Exception if startup fails
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting A2A Camel Component - Standalone Sample...");

        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();
        Main main = support.createMain("standalone/routes/*.yaml");
        main.run();
    }
}
