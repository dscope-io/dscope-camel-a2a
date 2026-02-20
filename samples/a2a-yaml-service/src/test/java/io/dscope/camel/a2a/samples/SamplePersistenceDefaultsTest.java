package io.dscope.camel.a2a.samples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SamplePersistenceDefaultsTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty(SamplePersistenceDefaults.PERSISTENCE_ENABLED_KEY);
        System.clearProperty(SamplePersistenceDefaults.PERSISTENCE_BACKEND_KEY);
        System.clearProperty(SamplePersistenceDefaults.REDIS_URI_KEY);
    }

    @Test
    void configureRedisPersistenceSetsRedisDefaults() {
        SamplePersistenceDefaults.configureRedisPersistence();

        assertEquals("true", System.getProperty(SamplePersistenceDefaults.PERSISTENCE_ENABLED_KEY));
        assertEquals("redis", System.getProperty(SamplePersistenceDefaults.PERSISTENCE_BACKEND_KEY));
        assertEquals(SamplePersistenceDefaults.DEFAULT_REDIS_URI,
            System.getProperty(SamplePersistenceDefaults.REDIS_URI_KEY));
    }

    @Test
    void configureRedisPersistenceDoesNotOverrideProvidedProperties() {
        System.setProperty(SamplePersistenceDefaults.PERSISTENCE_ENABLED_KEY, "true");
        System.setProperty(SamplePersistenceDefaults.PERSISTENCE_BACKEND_KEY, "jdbc");
        System.setProperty(SamplePersistenceDefaults.REDIS_URI_KEY, "redis://example:6380");

        SamplePersistenceDefaults.configureRedisPersistence();

        assertEquals("true", System.getProperty(SamplePersistenceDefaults.PERSISTENCE_ENABLED_KEY));
        assertEquals("jdbc", System.getProperty(SamplePersistenceDefaults.PERSISTENCE_BACKEND_KEY));
        assertEquals("redis://example:6380", System.getProperty(SamplePersistenceDefaults.REDIS_URI_KEY));
    }
}
