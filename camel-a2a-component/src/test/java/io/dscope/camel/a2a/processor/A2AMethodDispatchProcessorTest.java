package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardPolicyChecker;
import io.dscope.camel.a2a.catalog.AllowAllAgentCardSignatureVerifier;
import io.dscope.camel.a2a.catalog.DefaultAgentCardCatalog;
import io.dscope.camel.a2a.catalog.NoopAgentCardSigner;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryA2ATaskService;
import io.dscope.camel.a2a.service.InMemoryPushNotificationConfigService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class A2AMethodDispatchProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private DefaultCamelContext context;
    private A2AJsonRpcEnvelopeProcessor envelopeProcessor;
    private A2AMethodDispatchProcessor dispatchProcessor;
    private A2AErrorProcessor errorProcessor;
    private InMemoryTaskEventService eventService;

    @BeforeEach
    void setUp() {
        context = new DefaultCamelContext();
        eventService = new InMemoryTaskEventService();
        A2ATaskService taskService = new InMemoryA2ATaskService(eventService);
        InMemoryPushNotificationConfigService pushConfigService = new InMemoryPushNotificationConfigService((config, event, attemptNumber) -> {
            PushDeliveryAttempt attempt = new PushDeliveryAttempt();
            attempt.setConfigId(config.getConfigId());
            attempt.setEndpointUrl(config.getEndpointUrl());
            attempt.setAttemptNumber(attemptNumber);
            attempt.setStatusCode(202);
            attempt.setSuccess(true);
            return attempt;
        });
        eventService.addListener(pushConfigService::onTaskEvent);
        envelopeProcessor = new A2AJsonRpcEnvelopeProcessor(A2AProtocolMethods.CORE_METHODS);
        DefaultAgentCardCatalog agentCardCatalog = new DefaultAgentCardCatalog(
            "test-agent",
            "Test Agent",
            "Test description",
            "http://localhost:8081/a2a/rpc",
            new NoopAgentCardSigner(),
            new AllowAllAgentCardSignatureVerifier(),
            new AllowAllAgentCardPolicyChecker()
        );
        dispatchProcessor = new A2AMethodDispatchProcessor(Map.ofEntries(
            Map.entry(A2AProtocolMethods.SEND_MESSAGE, new SendMessageProcessor(taskService)),
            Map.entry(A2AProtocolMethods.SEND_STREAMING_MESSAGE, new SendStreamingMessageProcessor(taskService, eventService)),
            Map.entry(A2AProtocolMethods.GET_TASK, new GetTaskProcessor(taskService)),
            Map.entry(A2AProtocolMethods.LIST_TASKS, new ListTasksProcessor(taskService)),
            Map.entry(A2AProtocolMethods.CANCEL_TASK, new CancelTaskProcessor(taskService)),
            Map.entry(A2AProtocolMethods.SUBSCRIBE_TO_TASK, new SubscribeToTaskProcessor(taskService, eventService)),
            Map.entry(A2AProtocolMethods.CREATE_PUSH_NOTIFICATION_CONFIG, new CreatePushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.GET_PUSH_NOTIFICATION_CONFIG, new GetPushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.LIST_PUSH_NOTIFICATION_CONFIGS, new ListPushNotificationConfigsProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.DELETE_PUSH_NOTIFICATION_CONFIG, new DeletePushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.GET_EXTENDED_AGENT_CARD, new GetExtendedAgentCardProcessor(agentCardCatalog))
        ));
        errorProcessor = new A2AErrorProcessor();
    }

    @Test
    void sendGetListCancelFlowReturnsJsonRpcResults() throws Exception {
        JsonNode sendResponse = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello"}]},"metadata":{"source":"test"}},"id":"1"}
            """);
        String taskId = sendResponse.get("result").get("task").get("taskId").asText();
        assertFalse(taskId.isBlank());

        JsonNode getResponse = execute("""
            {"jsonrpc":"2.0","method":"GetTask","params":{"taskId":"%s"},"id":"2"}
            """.formatted(taskId));
        assertEquals(taskId, getResponse.get("result").get("task").get("taskId").asText());

        JsonNode listResponse = execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"limit":10},"id":"3"}
            """);
        assertTrue(listResponse.get("result").get("tasks").isArray());
        assertTrue(listResponse.get("result").get("tasks").size() >= 1);

        JsonNode cancelResponse = execute("""
            {"jsonrpc":"2.0","method":"CancelTask","params":{"taskId":"%s","reason":"user requested"},"id":"4"}
            """.formatted(taskId));
        assertTrue(cancelResponse.get("result").get("canceled").asBoolean());
        assertEquals("CANCELED", cancelResponse.get("result").get("task").get("status").get("state").asText());
    }

    @Test
    void unknownMethodReturnsMethodNotFoundError() throws Exception {
        JsonNode response = execute("""
            {"jsonrpc":"2.0","method":"NopeMethod","params":{},"id":"9"}
            """);

        assertEquals(-32601, response.get("error").get("code").asInt());
        assertEquals("9", response.get("id").asText());
    }

    @Test
    void invalidParamsReturnInvalidParamsError() throws Exception {
        JsonNode response = execute("""
            {"jsonrpc":"2.0","method":"GetTask","params":{},"id":"11"}
            """);

        assertEquals(-32602, response.get("error").get("code").asInt());
        assertEquals("11", response.get("id").asText());
        assertEquals("GetTask requires taskId", response.get("error").get("message").asText());
    }

    @Test
    void sendStreamingAndSubscribeProduceStreamMetadata() throws Exception {
        JsonNode streamingResponse = execute("""
            {"jsonrpc":"2.0","method":"SendStreamingMessage","params":{"message":{"messageId":"stream-m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello stream"}]}},"id":"21"}
            """);
        String taskId = streamingResponse.get("result").get("task").get("taskId").asText();
        String subscriptionId = streamingResponse.get("result").get("subscriptionId").asText();
        String streamUrl = streamingResponse.get("result").get("streamUrl").asText();

        assertFalse(taskId.isBlank());
        assertFalse(subscriptionId.isBlank());
        assertTrue(streamUrl.contains("/a2a/sse/" + taskId));

        JsonNode subscribeResponse = execute("""
            {"jsonrpc":"2.0","method":"SubscribeToTask","params":{"taskId":"%s","afterSequence":0,"limit":20},"id":"22"}
            """.formatted(taskId));
        assertTrue(subscribeResponse.get("result").get("terminal").asBoolean());
        assertTrue(subscribeResponse.get("result").get("streamUrl").asText().contains("/a2a/sse/" + taskId));
    }

    @Test
    void createListGetDeletePushConfigFlowWorks() throws Exception {
        JsonNode create = execute("""
            {"jsonrpc":"2.0","method":"CreatePushNotificationConfig","params":{"taskId":"t-1","endpointUrl":"https://example.test/hook","maxRetries":2,"retryBackoffMs":0},"id":"31"}
            """);
        String configId = create.get("result").get("config").get("configId").asText();
        assertFalse(configId.isBlank());

        JsonNode get = execute("""
            {"jsonrpc":"2.0","method":"GetPushNotificationConfig","params":{"configId":"%s"},"id":"32"}
            """.formatted(configId));
        assertEquals(configId, get.get("result").get("config").get("configId").asText());
        assertEquals("https://example.test/hook", get.get("result").get("config").get("endpointUrl").asText());

        JsonNode list = execute("""
            {"jsonrpc":"2.0","method":"ListPushNotificationConfigs","params":{"taskId":"t-1","limit":10},"id":"33"}
            """);
        assertTrue(list.get("result").get("configs").isArray());
        assertTrue(list.get("result").get("configs").size() >= 1);

        JsonNode delete = execute("""
            {"jsonrpc":"2.0","method":"DeletePushNotificationConfig","params":{"configId":"%s"},"id":"34"}
            """.formatted(configId));
        assertTrue(delete.get("result").get("deleted").asBoolean());
    }

    @Test
    void createPushConfigWithInvalidParamsReturnsInvalidParamsError() throws Exception {
        JsonNode response = execute("""
            {"jsonrpc":"2.0","method":"CreatePushNotificationConfig","params":{"endpointUrl":" "},"id":"35"}
            """);
        assertEquals(-32602, response.get("error").get("code").asInt());
        assertEquals("35", response.get("id").asText());
    }

    @Test
    void getExtendedAgentCardReturnsCapabilitiesAndSecurity() throws Exception {
        JsonNode response = execute("""
            {"jsonrpc":"2.0","method":"GetExtendedAgentCard","params":{"includeSignature":true},"id":"36"}
            """);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals("36", response.get("id").asText());
        JsonNode card = response.get("result").get("agentCard");
        assertEquals("test-agent", card.get("agentId").asText());
        assertTrue(card.get("capabilities").get("streaming").asBoolean());
        assertTrue(card.get("securitySchemes").has("bearerAuth"));
        assertTrue(card.get("metadata").get("extended").asBoolean());
    }

    @Test
    void allImplementedCoreMethodsAcceptContractPayloads() throws Exception {
        Set<String> implementedMethods = Set.of(
            A2AProtocolMethods.SEND_MESSAGE,
            A2AProtocolMethods.SEND_STREAMING_MESSAGE,
            A2AProtocolMethods.GET_TASK,
            A2AProtocolMethods.LIST_TASKS,
            A2AProtocolMethods.CANCEL_TASK,
            A2AProtocolMethods.SUBSCRIBE_TO_TASK,
            A2AProtocolMethods.CREATE_PUSH_NOTIFICATION_CONFIG,
            A2AProtocolMethods.GET_PUSH_NOTIFICATION_CONFIG,
            A2AProtocolMethods.LIST_PUSH_NOTIFICATION_CONFIGS,
            A2AProtocolMethods.DELETE_PUSH_NOTIFICATION_CONFIG,
            A2AProtocolMethods.GET_EXTENDED_AGENT_CARD
        );
        Set<String> expected = new HashSet<>(A2AProtocolMethods.CORE_METHODS);
        expected.remove(A2AProtocolMethods.LEGACY_INTENT_EXECUTE);
        assertEquals(expected, implementedMethods);

        JsonNode send = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"contract-m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello"}]}},"id":"c1"}
            """);
        String taskId = send.get("result").get("task").get("taskId").asText();

        JsonNode stream = execute("""
            {"jsonrpc":"2.0","method":"SendStreamingMessage","params":{"message":{"messageId":"contract-stream","role":"user","parts":[{"partId":"p1","type":"text","text":"stream"}]}},"id":"c2"}
            """);
        String streamingTaskId = stream.get("result").get("task").get("taskId").asText();
        String configTaskId = taskId.isBlank() ? streamingTaskId : taskId;

        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"GetTask","params":{"taskId":"%s"},"id":"c3"}
            """.formatted(taskId)).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"limit":10},"id":"c4"}
            """).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"CancelTask","params":{"taskId":"%s","reason":"contract"},"id":"c5"}
            """.formatted(taskId)).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"SubscribeToTask","params":{"taskId":"%s","afterSequence":0,"limit":25},"id":"c6"}
            """.formatted(streamingTaskId)).get("jsonrpc").asText());

        JsonNode create = execute("""
            {"jsonrpc":"2.0","method":"CreatePushNotificationConfig","params":{"taskId":"%s","endpointUrl":"https://example.test/contract","maxRetries":1,"retryBackoffMs":0},"id":"c7"}
            """.formatted(configTaskId));
        String configId = create.get("result").get("config").get("configId").asText();

        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"GetPushNotificationConfig","params":{"configId":"%s"},"id":"c8"}
            """.formatted(configId)).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"ListPushNotificationConfigs","params":{"limit":10},"id":"c9"}
            """).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"DeletePushNotificationConfig","params":{"configId":"%s"},"id":"c10"}
            """.formatted(configId)).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"GetExtendedAgentCard","params":{"includeSignature":false},"id":"c11"}
            """).get("jsonrpc").asText());
    }

    private JsonNode execute(String payload) throws Exception {
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(payload);
        try {
            envelopeProcessor.process(exchange);
            dispatchProcessor.process(exchange);
        } catch (Exception e) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
            errorProcessor.process(exchange);
        }
        return mapper.readTree(exchange.getMessage().getBody(String.class));
    }
}
