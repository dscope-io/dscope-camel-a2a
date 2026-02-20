package io.dscope.camel.a2a.samples;

/**
 * Configures persistence defaults for sample applications.
 */
public final class SamplePersistenceDefaults {

    static final String PERSISTENCE_ENABLED_KEY = "camel.persistence.enabled";
    static final String PERSISTENCE_BACKEND_KEY = "camel.persistence.backend";
    static final String REDIS_URI_KEY = "camel.persistence.redis.uri";
    static final String REDIS_URI_ENV = "REDIS_URI";
    static final String DEFAULT_REDIS_URI = "redis://localhost:6379";

    private SamplePersistenceDefaults() {
    }

    public static void configureRedisPersistence() {
        System.setProperty(PERSISTENCE_ENABLED_KEY,
            System.getProperty(PERSISTENCE_ENABLED_KEY, "true"));
        System.setProperty(PERSISTENCE_BACKEND_KEY,
            System.getProperty(PERSISTENCE_BACKEND_KEY, "redis"));

        String redisUri = System.getProperty(REDIS_URI_KEY);
        if (redisUri != null && !redisUri.isBlank()) {
            return;
        }

        String envRedisUri = System.getenv(REDIS_URI_ENV);
        if (envRedisUri != null && !envRedisUri.isBlank()) {
            System.setProperty(REDIS_URI_KEY, envRedisUri);
            return;
        }

        System.setProperty(REDIS_URI_KEY, DEFAULT_REDIS_URI);
    }
}
