package io.dscope.camel.a2a.samples;

/**
 * Main entry point for A2A Camel component samples.
 * This class provides a centralized way to run different sample applications.
 */
public class Main {

    /**
     * Main entry point for running samples.
     * Usage: java Main [sample-name]
     * Available samples: basic (default), standalone
     *
     * @param args command line arguments - first arg specifies sample type
     * @throws Exception if sample execution fails
     */
    public static void main(String[] args) throws Exception {
        String sampleType = args.length > 0 ? args[0] : "basic";

        System.out.println("Starting A2A Camel Component Samples...");
        System.out.println("Running sample: " + sampleType);

        switch (sampleType.toLowerCase()) {
            case "basic" -> {
                System.out.println("Running basic A2A platform sample...");
                io.dscope.camel.a2a.samples.basic.Runner.main(args);
            }
            case "standalone" -> {
                System.out.println("Running standalone A2A sample...");
                io.dscope.camel.a2a.samples.standalone.Runner.main(args);
            }
            default -> {
                System.out.println("Unknown sample type: " + sampleType);
                System.out.println("Available samples: basic, standalone");
                System.exit(1);
            }
        }
    }
}