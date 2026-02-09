package io.dscope.camel.a2a.model;

/**
 * Aggregated push delivery counters.
 */
public class PushDeliveryStats {

    private long attempts;
    private long successes;
    private long failures;

    public long getAttempts() {
        return attempts;
    }

    public void setAttempts(long attempts) {
        this.attempts = attempts;
    }

    public long getSuccesses() {
        return successes;
    }

    public void setSuccesses(long successes) {
        this.successes = successes;
    }

    public long getFailures() {
        return failures;
    }

    public void setFailures(long failures) {
        this.failures = failures;
    }
}
