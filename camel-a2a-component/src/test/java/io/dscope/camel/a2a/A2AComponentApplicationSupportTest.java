package io.dscope.camel.a2a;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import io.dscope.camel.a2a.service.PersistentA2ATaskEventService;
import io.dscope.camel.a2a.service.PersistentA2ATaskService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AComponentApplicationSupportTest {

    @Test
    void bindDefaultBeansRegistersCoreProcessorsAndCatalog() {
        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();
        Map<String, Object> beans = new LinkedHashMap<>();

        support.bindDefaultBeans(beans::put);

        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_ENVELOPE_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_ERROR_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_METHOD_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_TASK_SERVICE));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_SEND_MESSAGE_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_GET_TASK_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_LIST_TASKS_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_CANCEL_TASK_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_SEND_STREAMING_MESSAGE_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_SUBSCRIBE_TO_TASK_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_SSE_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_TASK_EVENT_SERVICE));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_PUSH_CONFIG_SERVICE));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_CREATE_PUSH_CONFIG_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_GET_PUSH_CONFIG_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_LIST_PUSH_CONFIGS_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_DELETE_PUSH_CONFIG_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_AGENT_CARD_SIGNER));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_AGENT_CARD_VERIFIER));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_AGENT_CARD_POLICY_CHECKER));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_AGENT_CARD_CATALOG));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_GET_EXTENDED_AGENT_CARD_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_AGENT_CARD_DISCOVERY_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_DIAGNOSTICS_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_OUTGOING_PROCESSOR));
        assertTrue(beans.containsKey(A2AComponentApplicationSupport.BEAN_TOOL_REGISTRY));
    }

    @Test
    void validateRouteIncludePatternRejectsBlankPattern() {
        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> support.validateRouteIncludePattern("   "));

        assertEquals("Route include pattern must not be blank", error.getMessage());
    }

    @Test
    void validateRouteIncludePatternRejectsNonYamlPattern() {
        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> support.validateRouteIncludePattern("basic/routes/*.xml"));

        assertEquals("Route include pattern must target .yaml or .yml resources", error.getMessage());
    }

    @Test
    void createMainAppliesExtensionHook() {
        A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();
        AtomicBoolean hookCalled = new AtomicBoolean(false);

        var main = support.createMain("basic/routes/*.camel.yaml", m -> hookCalled.set(true));

        assertNotNull(main);
        assertTrue(hookCalled.get());
    }

    @Test
    void bindDefaultBeansUsesPersistentServicesWhenPersistenceEnabled() {
        String dbName = "a2aSupportTest" + java.util.UUID.randomUUID().toString().replace("-", "");
        String jdbcUrl = "jdbc:derby:memory:" + dbName + ";create=true";

        String prevEnabled = System.getProperty("camel.persistence.enabled");
        String prevBackend = System.getProperty("camel.persistence.backend");
        String prevJdbcUrl = System.getProperty("camel.persistence.jdbc.url");
        String prevJdbcUser = System.getProperty("camel.persistence.jdbc.user");
        String prevJdbcPassword = System.getProperty("camel.persistence.jdbc.password");

        try {
            System.setProperty("camel.persistence.enabled", "true");
            System.setProperty("camel.persistence.backend", "jdbc");
            System.setProperty("camel.persistence.jdbc.url", jdbcUrl);
            System.setProperty("camel.persistence.jdbc.user", "");
            System.setProperty("camel.persistence.jdbc.password", "");

            A2AComponentApplicationSupport support = new A2AComponentApplicationSupport();
            Map<String, Object> beans = new LinkedHashMap<>();

            support.bindDefaultBeans(beans::put);

            assertInstanceOf(PersistentA2ATaskService.class, beans.get(A2AComponentApplicationSupport.BEAN_TASK_SERVICE));
            assertInstanceOf(PersistentA2ATaskEventService.class, beans.get(A2AComponentApplicationSupport.BEAN_TASK_EVENT_SERVICE));
        } finally {
            restoreOrClear("camel.persistence.enabled", prevEnabled);
            restoreOrClear("camel.persistence.backend", prevBackend);
            restoreOrClear("camel.persistence.jdbc.url", prevJdbcUrl);
            restoreOrClear("camel.persistence.jdbc.user", prevJdbcUser);
            restoreOrClear("camel.persistence.jdbc.password", prevJdbcPassword);
        }
    }

    private void restoreOrClear(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
