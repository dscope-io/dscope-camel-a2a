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
        envelopeProcessor = new A2AJsonRpcEnvelopeProcessor(A2AProtocolMethods.SUPPORTED_METHODS);
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
            Map.entry(A2AProtocolMethods.CREATE_TASK_PUSH_NOTIFICATION_CONFIG, new CreatePushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.GET_PUSH_NOTIFICATION_CONFIG, new GetPushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.GET_TASK_PUSH_NOTIFICATION_CONFIG, new GetPushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.LIST_PUSH_NOTIFICATION_CONFIGS, new ListPushNotificationConfigsProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.LIST_TASK_PUSH_NOTIFICATION_CONFIGS, new ListPushNotificationConfigsProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.DELETE_PUSH_NOTIFICATION_CONFIG, new DeletePushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.DELETE_TASK_PUSH_NOTIFICATION_CONFIG, new DeletePushNotificationConfigProcessor(pushConfigService)),
            Map.entry(A2AProtocolMethods.GET_EXTENDED_AGENT_CARD, new GetExtendedAgentCardProcessor(agentCardCatalog))
        ));
        errorProcessor = new A2AErrorProcessor();
    }

    @Test
    void sendGetListCancelFlowReturnsJsonRpcResults() throws Exception {
        JsonNode sendResponse = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello"}]},"metadata":{"source":"test"}},"id":"1"}
            """);
        String taskId = sendResponse.get("result").get("id").asText();
        assertFalse(taskId.isBlank());

        JsonNode getResponse = execute("""
            {"jsonrpc":"2.0","method":"GetTask","params":{"id":"%s"},"id":"2"}
            """.formatted(taskId));
        assertEquals(taskId, getResponse.get("result").get("id").asText());

        JsonNode listResponse = execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"pageSize":10},"id":"3"}
            """);
        assertTrue(listResponse.get("result").get("tasks").isArray());
        assertTrue(listResponse.get("result").get("tasks").size() >= 1);

        JsonNode cancelResponse = execute("""
            {"jsonrpc":"2.0","method":"CancelTask","params":{"taskId":"%s","reason":"user requested"},"id":"4"}
            """.formatted(taskId));
        assertEquals("TASK_STATE_CANCELED", cancelResponse.get("result").get("status").get("state").asText());
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
    void sendStreamingAndSubscribeReturnSseStatusUpdates() throws Exception {
        Exchange streamingExchange = executeExchange("""
            {"jsonrpc":"2.0","method":"SendStreamingMessage","params":{"message":{"messageId":"stream-m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello stream"}]}},"id":"21"}
            """);
        assertEquals("text/event-stream", streamingExchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
        var streamingResponses = readSseResponses(streamingExchange);
        assertTrue(streamingResponses.size() >= 4);
        String taskId = streamingResponses.get(0).path("result").path("statusUpdate").path("taskId").asText();

        assertFalse(taskId.isBlank());
        assertEquals("TASK_STATE_COMPLETED",
            streamingResponses.get(streamingResponses.size() - 1).path("result")
                .path("statusUpdate").path("status").path("state").asText());

        JsonNode send = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"subscribe-m1","role":"user","parts":[{"text":"still running"}]}},"id":"subscribe-send"}
            """);
        String activeTaskId = send.path("result").path("id").asText();

        Exchange subscribeExchange = executeExchange("""
            {"jsonrpc":"2.0","method":"SubscribeToTask","params":{"id":"%s"},"id":"22"}
            """.formatted(activeTaskId));
        assertEquals("text/event-stream", subscribeExchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
        assertEquals(activeTaskId, readSseResponse(subscribeExchange).path("result")
            .path("statusUpdate").path("taskId").asText());

        JsonNode terminalSubscribe = execute("""
            {"jsonrpc":"2.0","method":"SubscribeToTask","params":{"id":"%s"},"id":"23"}
            """.formatted(taskId));
        assertEquals(-32004, terminalSubscribe.path("error").path("code").asInt());
    }

    @Test
    void listTasksFiltersAndPaginatesWithProtocolFields() throws Exception {
        String contextId = "context-page";
        String firstTaskId = createTask("page-1", contextId);
        String secondTaskId = createTask("page-2", contextId);

        JsonNode firstPage = execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"contextId":"%s","pageSize":1,"historyLength":0},"id":"page-1"}
            """.formatted(contextId));
        JsonNode firstResult = firstPage.path("result");
        assertEquals(2, firstResult.path("totalSize").asInt());
        assertEquals(1, firstResult.path("pageSize").asInt());
        assertEquals(1, firstResult.path("tasks").size());
        assertFalse(firstResult.path("nextPageToken").asText().isBlank());
        assertEquals(contextId, firstResult.path("tasks").get(0).path("contextId").asText());
        assertEquals(0, firstResult.path("tasks").get(0).path("history").size());

        JsonNode secondPage = execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"contextId":"%s","pageSize":1,"pageToken":"%s"},"id":"page-2"}
            """.formatted(contextId, firstResult.path("nextPageToken").asText()));
        JsonNode secondResult = secondPage.path("result");
        assertEquals(1, secondResult.path("tasks").size());
        assertTrue(secondResult.path("nextPageToken").asText().isBlank());
        assertNotEquals(firstResult.path("tasks").get(0).path("id").asText(),
            secondResult.path("tasks").get(0).path("id").asText());
        assertTrue(Set.of(firstTaskId, secondTaskId).contains(secondResult.path("tasks").get(0).path("id").asText()));
    }

    @Test
    void createListGetDeletePushConfigFlowWorks() throws Exception {
        JsonNode create = execute("""
            {"jsonrpc":"2.0","method":"CreatePushNotificationConfig","params":{"taskId":"t-1","endpointUrl":"https://example.test/hook","maxRetries":2,"retryBackoffMs":0},"id":"31"}
            """);
        String configId = create.get("result").get("id").asText();
        assertFalse(configId.isBlank());

        JsonNode get = execute("""
            {"jsonrpc":"2.0","method":"GetPushNotificationConfig","params":{"configId":"%s"},"id":"32"}
            """.formatted(configId));
        assertEquals(configId, get.get("result").get("id").asText());
        assertEquals("https://example.test/hook", get.get("result").get("url").asText());

        JsonNode list = execute("""
            {"jsonrpc":"2.0","method":"ListPushNotificationConfigs","params":{"taskId":"t-1","limit":10},"id":"33"}
            """);
        assertTrue(list.get("result").get("configs").isArray());
        assertTrue(list.get("result").get("configs").size() >= 1);

        JsonNode delete = execute("""
            {"jsonrpc":"2.0","method":"DeletePushNotificationConfig","params":{"configId":"%s"},"id":"34"}
            """.formatted(configId));
        assertTrue(delete.get("result").isObject());
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
    void a2a101RequestFieldsAndPushMethodNamesAreAccepted() throws Exception {
        JsonNode send = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"a2a-1","role":"ROLE_USER","parts":[{"text":"hello"}]},"configuration":{"acceptedOutputModes":["text/plain"],"returnImmediately":true}},"id":"a2a-send"}
            """);
        String taskId = send.path("result").path("id").asText();
        assertFalse(taskId.isBlank());
        assertFalse(send.path("result").path("contextId").asText().isBlank());

        JsonNode create = execute("""
            {"jsonrpc":"2.0","method":"CreateTaskPushNotificationConfig","params":{"taskId":"%s","id":"cfg-a2a","url":"https://example.test/a2a","token":"token"},"id":"a2a-create"}
            """.formatted(taskId));
        assertEquals("cfg-a2a", create.path("result").path("id").asText());
        assertEquals("https://example.test/a2a", create.path("result").path("url").asText());

        JsonNode get = execute("""
            {"jsonrpc":"2.0","method":"GetTaskPushNotificationConfig","params":{"taskId":"%s","id":"cfg-a2a"},"id":"a2a-get"}
            """.formatted(taskId));
        assertEquals("cfg-a2a", get.path("result").path("id").asText());

        JsonNode list = execute("""
            {"jsonrpc":"2.0","method":"ListTaskPushNotificationConfigs","params":{"taskId":"%s","pageSize":10},"id":"a2a-list"}
            """.formatted(taskId));
        assertTrue(list.path("result").path("configs").isArray());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"DeleteTaskPushNotificationConfig","params":{"taskId":"%s","id":"cfg-a2a"},"id":"a2a-delete"}
            """.formatted(taskId)).path("jsonrpc").asText());
    }

    @Test
    void getExtendedAgentCardReturnsCapabilitiesAndSecurity() throws Exception {
        JsonNode response = execute("""
            {"jsonrpc":"2.0","method":"GetExtendedAgentCard","params":{"includeSignature":true},"id":"36"}
            """);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals("36", response.get("id").asText());
        JsonNode card = response.get("result");
        assertEquals("http://localhost:8081/a2a/rpc",
            card.get("supportedInterfaces").get(0).get("url").asText());
        assertTrue(card.get("capabilities").get("streaming").asBoolean());
        assertTrue(card.get("securitySchemes").has("bearerAuth"));
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
            A2AProtocolMethods.GET_EXTENDED_AGENT_CARD,
            A2AProtocolMethods.CREATE_TASK_PUSH_NOTIFICATION_CONFIG,
            A2AProtocolMethods.GET_TASK_PUSH_NOTIFICATION_CONFIG,
            A2AProtocolMethods.LIST_TASK_PUSH_NOTIFICATION_CONFIGS,
            A2AProtocolMethods.DELETE_TASK_PUSH_NOTIFICATION_CONFIG
        );
        Set<String> expected = new HashSet<>(A2AProtocolMethods.CORE_METHODS);
        assertEquals(expected, implementedMethods);

        JsonNode send = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"contract-m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello"}]}},"id":"c1"}
            """);
        String taskId = send.get("result").get("id").asText();

        JsonNode stream = execute("""
            {"jsonrpc":"2.0","method":"SendStreamingMessage","params":{"message":{"messageId":"contract-stream","role":"user","parts":[{"partId":"p1","type":"text","text":"stream"}]}},"id":"c2"}
            """);
        String streamingTaskId = stream.path("result").path("statusUpdate").path("taskId").asText();
        String configTaskId = taskId.isBlank() ? streamingTaskId : taskId;

        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"GetTask","params":{"id":"%s"},"id":"c3"}
            """.formatted(taskId)).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"ListTasks","params":{"limit":10},"id":"c4"}
            """).get("jsonrpc").asText());
        assertEquals("2.0", execute("""
            {"jsonrpc":"2.0","method":"CancelTask","params":{"taskId":"%s","reason":"contract"},"id":"c5"}
            """.formatted(taskId)).get("jsonrpc").asText());
        JsonNode subscribableTask = execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"contract-subscribe","role":"user","parts":[{"text":"subscribe me"}]}},"id":"c6-create"}
            """);
        String subscribableTaskId = subscribableTask.path("result").path("id").asText();
        assertEquals("text/event-stream", executeExchange("""
            {"jsonrpc":"2.0","method":"SubscribeToTask","params":{"id":"%s"},"id":"c6"}
            """.formatted(subscribableTaskId)).getMessage().getHeader(Exchange.CONTENT_TYPE));

        JsonNode create = execute("""
            {"jsonrpc":"2.0","method":"CreatePushNotificationConfig","params":{"taskId":"%s","endpointUrl":"https://example.test/contract","maxRetries":1,"retryBackoffMs":0},"id":"c7"}
            """.formatted(configTaskId));
        String configId = create.get("result").get("id").asText();

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
        Exchange exchange = executeExchange(payload);
        String responseBody = exchange.getMessage().getBody(String.class);
        responseBody = firstSseData(responseBody);
        return mapper.readTree(responseBody);
    }

    private String createTask(String messageId, String contextId) throws Exception {
        return execute("""
            {"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"%s","contextId":"%s","role":"user","parts":[{"text":"page task"}]}},"id":"%s"}
            """.formatted(messageId, contextId, messageId)).path("result").path("id").asText();
    }

    private Exchange executeExchange(String payload) throws Exception {
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(payload);
        try {
            envelopeProcessor.process(exchange);
            dispatchProcessor.process(exchange);
        } catch (Exception e) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
            errorProcessor.process(exchange);
        }
        return exchange;
    }

    private JsonNode readSseResponse(Exchange exchange) throws Exception {
        String body = exchange.getMessage().getBody(String.class);
        return mapper.readTree(firstSseData(body));
    }

    private java.util.List<JsonNode> readSseResponses(Exchange exchange) throws Exception {
        String body = exchange.getMessage().getBody(String.class);
        return java.util.Arrays.stream(body.split("\\n\\n"))
            .filter(event -> event.startsWith("data: "))
            .map(event -> event.substring("data: ".length()))
            .map(data -> {
                try {
                    return mapper.readTree(data);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            })
            .toList();
    }

    private String firstSseData(String body) {
        if (!body.startsWith("data: ")) {
            return body;
        }
        return body.substring("data: ".length(), body.indexOf("\n\n")).trim();
    }
}
