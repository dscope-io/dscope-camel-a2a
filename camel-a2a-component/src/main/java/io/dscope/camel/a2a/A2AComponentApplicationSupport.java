package io.dscope.camel.a2a;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.Consumer;
import java.util.Map;
import io.dscope.camel.a2a.catalog.AgentCardCatalog;
import io.dscope.camel.a2a.catalog.AgentCardPolicyChecker;
import io.dscope.camel.a2a.catalog.AgentCardSignatureVerifier;
import io.dscope.camel.a2a.catalog.AgentCardSigner;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardPolicyChecker;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardSignatureVerifier;
import io.dscope.camel.a2a.catalog.A2AToolRegistry;
import io.dscope.camel.a2a.catalog.DefaultAgentCardCatalog;
import io.dscope.camel.a2a.catalog.NoopAgentCardSigner;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import io.dscope.camel.a2a.processor.A2AMethodDispatchProcessor;
import io.dscope.camel.a2a.processor.A2ADiagnosticsProcessor;
import io.dscope.camel.a2a.processor.A2AErrorProcessor;
import io.dscope.camel.a2a.processor.A2AJsonRpcEnvelopeProcessor;
import io.dscope.camel.a2a.processor.A2AOutgoingMessageProcessor;
import io.dscope.camel.a2a.processor.A2ATaskSseProcessor;
import io.dscope.camel.a2a.processor.AgentCardDiscoveryProcessor;
import io.dscope.camel.a2a.processor.CancelTaskProcessor;
import io.dscope.camel.a2a.processor.CreatePushNotificationConfigProcessor;
import io.dscope.camel.a2a.processor.DeletePushNotificationConfigProcessor;
import io.dscope.camel.a2a.processor.GetExtendedAgentCardProcessor;
import io.dscope.camel.a2a.processor.GetTaskProcessor;
import io.dscope.camel.a2a.processor.GetPushNotificationConfigProcessor;
import io.dscope.camel.a2a.processor.IntentRouterProcessor;
import io.dscope.camel.a2a.processor.ListTasksProcessor;
import io.dscope.camel.a2a.processor.ListPushNotificationConfigsProcessor;
import io.dscope.camel.a2a.processor.SendStreamingMessageProcessor;
import io.dscope.camel.a2a.processor.SendMessageProcessor;
import io.dscope.camel.a2a.processor.SubscribeToTaskProcessor;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryA2ATaskService;
import io.dscope.camel.a2a.service.InMemoryPushNotificationConfigService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;
import io.dscope.camel.a2a.service.WebhookPushNotificationNotifier;
import org.apache.camel.main.Main;
import org.apache.camel.Processor;

/**
 * Standardized bootstrap support for A2A sample and service applications.
 */
public class A2AComponentApplicationSupport {

    private static final Pattern YAML_ROUTE_PATTERN = Pattern.compile(".*\\.(ya?ml)$");

    public static final String BEAN_ENVELOPE_PROCESSOR = "a2aJsonRpcEnvelopeProcessor";
    public static final String BEAN_ERROR_PROCESSOR = "a2aErrorProcessor";
    public static final String BEAN_METHOD_PROCESSOR = "a2aMethodDispatchProcessor";
    public static final String BEAN_LEGACY_METHOD_PROCESSOR = "a2aIntentRouterProcessor";
    public static final String BEAN_OUTGOING_PROCESSOR = "a2aOutgoingMessageProcessor";
    public static final String BEAN_TOOL_REGISTRY = "a2aToolRegistry";
    public static final String BEAN_TASK_SERVICE = "a2aTaskService";
    public static final String BEAN_SEND_MESSAGE_PROCESSOR = "a2aSendMessageProcessor";
    public static final String BEAN_GET_TASK_PROCESSOR = "a2aGetTaskProcessor";
    public static final String BEAN_LIST_TASKS_PROCESSOR = "a2aListTasksProcessor";
    public static final String BEAN_CANCEL_TASK_PROCESSOR = "a2aCancelTaskProcessor";
    public static final String BEAN_SEND_STREAMING_MESSAGE_PROCESSOR = "a2aSendStreamingMessageProcessor";
    public static final String BEAN_SUBSCRIBE_TO_TASK_PROCESSOR = "a2aSubscribeToTaskProcessor";
    public static final String BEAN_SSE_PROCESSOR = "a2aTaskSseProcessor";
    public static final String BEAN_TASK_EVENT_SERVICE = "a2aTaskEventService";
    public static final String BEAN_PUSH_CONFIG_SERVICE = "a2aPushConfigService";
    public static final String BEAN_CREATE_PUSH_CONFIG_PROCESSOR = "a2aCreatePushConfigProcessor";
    public static final String BEAN_GET_PUSH_CONFIG_PROCESSOR = "a2aGetPushConfigProcessor";
    public static final String BEAN_LIST_PUSH_CONFIGS_PROCESSOR = "a2aListPushConfigsProcessor";
    public static final String BEAN_DELETE_PUSH_CONFIG_PROCESSOR = "a2aDeletePushConfigProcessor";
    public static final String BEAN_AGENT_CARD_CATALOG = "a2aAgentCardCatalog";
    public static final String BEAN_AGENT_CARD_SIGNER = "a2aAgentCardSigner";
    public static final String BEAN_AGENT_CARD_VERIFIER = "a2aAgentCardVerifier";
    public static final String BEAN_AGENT_CARD_POLICY_CHECKER = "a2aAgentCardPolicyChecker";
    public static final String BEAN_AGENT_CARD_DISCOVERY_PROCESSOR = "a2aAgentCardDiscoveryProcessor";
    public static final String BEAN_GET_EXTENDED_AGENT_CARD_PROCESSOR = "a2aGetExtendedAgentCardProcessor";
    public static final String BEAN_DIAGNOSTICS_PROCESSOR = "a2aDiagnosticsProcessor";

    @FunctionalInterface
    public interface BeanBinder {
        void bind(String name, Object bean);
    }

    /**
     * Creates and configures a Camel {@link Main} instance.
     *
     * @param routeIncludePattern route include pattern (e.g. "basic/routes/*.yaml")
     * @return configured main runtime
     */
    public Main createMain(String routeIncludePattern) {
        return createMain(routeIncludePattern, null);
    }

    /**
     * Creates and configures a Camel {@link Main} instance and applies an extension hook.
     *
     * @param routeIncludePattern route include pattern (e.g. "basic/routes/*.yaml")
     * @param customizer optional customizer for sample-specific wiring
     * @return configured main runtime
     */
    public Main createMain(String routeIncludePattern, Consumer<Main> customizer) {
        validateRouteIncludePattern(routeIncludePattern);

        Main main = new Main();
        bindDefaultBeans(main::bind);
        if (customizer != null) {
            customizer.accept(main);
        }
        main.configure().withRoutesIncludePattern(routeIncludePattern);
        return main;
    }

    /**
     * Binds default A2A protocol beans.
     */
    public void bindDefaultBeans(BeanBinder binder) {
        Objects.requireNonNull(binder, "binder must not be null");
        InMemoryTaskEventService taskEventService = new InMemoryTaskEventService();
        A2ATaskService taskService = new InMemoryA2ATaskService(taskEventService);
        A2APushNotificationConfigService pushConfigService =
            new InMemoryPushNotificationConfigService(new WebhookPushNotificationNotifier());
        taskEventService.addListener(pushConfigService::onTaskEvent);
        AgentCardSigner cardSigner = new NoopAgentCardSigner();
        AgentCardSignatureVerifier cardVerifier = new AllowAllAgentCardSignatureVerifier();
        AgentCardPolicyChecker cardPolicyChecker = new AllowAllAgentCardPolicyChecker();
        AgentCardCatalog agentCardCatalog = new DefaultAgentCardCatalog(
            "camel-a2a-agent",
            "Camel A2A Agent",
            "Apache Camel A2A compatibility service",
            "http://localhost:8081/a2a/rpc",
            cardSigner,
            cardVerifier,
            cardPolicyChecker
        );

        SendMessageProcessor sendMessageProcessor = new SendMessageProcessor(taskService);
        GetTaskProcessor getTaskProcessor = new GetTaskProcessor(taskService);
        ListTasksProcessor listTasksProcessor = new ListTasksProcessor(taskService);
        CancelTaskProcessor cancelTaskProcessor = new CancelTaskProcessor(taskService);
        SendStreamingMessageProcessor sendStreamingMessageProcessor =
            new SendStreamingMessageProcessor(taskService, taskEventService);
        SubscribeToTaskProcessor subscribeToTaskProcessor =
            new SubscribeToTaskProcessor(taskService, taskEventService);
        A2ATaskSseProcessor taskSseProcessor = new A2ATaskSseProcessor(taskEventService);
        CreatePushNotificationConfigProcessor createPushConfigProcessor =
            new CreatePushNotificationConfigProcessor(pushConfigService);
        GetPushNotificationConfigProcessor getPushConfigProcessor =
            new GetPushNotificationConfigProcessor(pushConfigService);
        ListPushNotificationConfigsProcessor listPushConfigsProcessor =
            new ListPushNotificationConfigsProcessor(pushConfigService);
        DeletePushNotificationConfigProcessor deletePushConfigProcessor =
            new DeletePushNotificationConfigProcessor(pushConfigService);
        GetExtendedAgentCardProcessor getExtendedAgentCardProcessor =
            new GetExtendedAgentCardProcessor(agentCardCatalog);
        AgentCardDiscoveryProcessor agentCardDiscoveryProcessor =
            new AgentCardDiscoveryProcessor(agentCardCatalog);
        A2ADiagnosticsProcessor diagnosticsProcessor =
            new A2ADiagnosticsProcessor(taskService, taskEventService, pushConfigService);

        Map<String, Processor> methodMap = Map.ofEntries(
            Map.entry(A2AProtocolMethods.SEND_MESSAGE, sendMessageProcessor),
            Map.entry(A2AProtocolMethods.SEND_STREAMING_MESSAGE, sendStreamingMessageProcessor),
            Map.entry(A2AProtocolMethods.GET_TASK, getTaskProcessor),
            Map.entry(A2AProtocolMethods.LIST_TASKS, listTasksProcessor),
            Map.entry(A2AProtocolMethods.CANCEL_TASK, cancelTaskProcessor),
            Map.entry(A2AProtocolMethods.SUBSCRIBE_TO_TASK, subscribeToTaskProcessor),
            Map.entry(A2AProtocolMethods.CREATE_PUSH_NOTIFICATION_CONFIG, createPushConfigProcessor),
            Map.entry(A2AProtocolMethods.GET_PUSH_NOTIFICATION_CONFIG, getPushConfigProcessor),
            Map.entry(A2AProtocolMethods.LIST_PUSH_NOTIFICATION_CONFIGS, listPushConfigsProcessor),
            Map.entry(A2AProtocolMethods.DELETE_PUSH_NOTIFICATION_CONFIG, deletePushConfigProcessor),
            Map.entry(A2AProtocolMethods.GET_EXTENDED_AGENT_CARD, getExtendedAgentCardProcessor)
        );

        binder.bind(BEAN_TASK_EVENT_SERVICE, taskEventService);
        binder.bind(BEAN_PUSH_CONFIG_SERVICE, pushConfigService);
        binder.bind(BEAN_AGENT_CARD_SIGNER, cardSigner);
        binder.bind(BEAN_AGENT_CARD_VERIFIER, cardVerifier);
        binder.bind(BEAN_AGENT_CARD_POLICY_CHECKER, cardPolicyChecker);
        binder.bind(BEAN_AGENT_CARD_CATALOG, agentCardCatalog);
        binder.bind(BEAN_TASK_SERVICE, taskService);
        binder.bind(BEAN_SEND_MESSAGE_PROCESSOR, sendMessageProcessor);
        binder.bind(BEAN_GET_TASK_PROCESSOR, getTaskProcessor);
        binder.bind(BEAN_LIST_TASKS_PROCESSOR, listTasksProcessor);
        binder.bind(BEAN_CANCEL_TASK_PROCESSOR, cancelTaskProcessor);
        binder.bind(BEAN_SEND_STREAMING_MESSAGE_PROCESSOR, sendStreamingMessageProcessor);
        binder.bind(BEAN_SUBSCRIBE_TO_TASK_PROCESSOR, subscribeToTaskProcessor);
        binder.bind(BEAN_SSE_PROCESSOR, taskSseProcessor);
        binder.bind(BEAN_CREATE_PUSH_CONFIG_PROCESSOR, createPushConfigProcessor);
        binder.bind(BEAN_GET_PUSH_CONFIG_PROCESSOR, getPushConfigProcessor);
        binder.bind(BEAN_LIST_PUSH_CONFIGS_PROCESSOR, listPushConfigsProcessor);
        binder.bind(BEAN_DELETE_PUSH_CONFIG_PROCESSOR, deletePushConfigProcessor);
        binder.bind(BEAN_GET_EXTENDED_AGENT_CARD_PROCESSOR, getExtendedAgentCardProcessor);
        binder.bind(BEAN_AGENT_CARD_DISCOVERY_PROCESSOR, agentCardDiscoveryProcessor);
        binder.bind(BEAN_DIAGNOSTICS_PROCESSOR, diagnosticsProcessor);
        binder.bind(BEAN_ENVELOPE_PROCESSOR, new A2AJsonRpcEnvelopeProcessor(A2AProtocolMethods.CORE_METHODS));
        binder.bind(BEAN_ERROR_PROCESSOR, new A2AErrorProcessor());
        binder.bind(BEAN_METHOD_PROCESSOR, new A2AMethodDispatchProcessor(methodMap));
        binder.bind(BEAN_LEGACY_METHOD_PROCESSOR, new IntentRouterProcessor());
        binder.bind(BEAN_OUTGOING_PROCESSOR, new A2AOutgoingMessageProcessor());
        binder.bind(BEAN_TOOL_REGISTRY, new A2AToolRegistry());
    }

    /**
     * Validates that the include pattern is non-empty and targets YAML routes.
     */
    public void validateRouteIncludePattern(String routeIncludePattern) {
        if (routeIncludePattern == null || routeIncludePattern.isBlank()) {
            throw new IllegalArgumentException("Route include pattern must not be blank");
        }
        if (!YAML_ROUTE_PATTERN.matcher(routeIncludePattern).matches()) {
            throw new IllegalArgumentException("Route include pattern must target .yaml or .yml resources");
        }
    }
}
