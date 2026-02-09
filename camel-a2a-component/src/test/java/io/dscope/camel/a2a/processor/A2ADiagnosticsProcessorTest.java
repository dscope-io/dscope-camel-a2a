package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Part;
import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.service.InMemoryA2ATaskService;
import io.dscope.camel.a2a.service.InMemoryPushNotificationConfigService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class A2ADiagnosticsProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void processReturnsDiagnosticsPayload() throws Exception {
        InMemoryTaskEventService eventService = new InMemoryTaskEventService();
        InMemoryPushNotificationConfigService pushService = new InMemoryPushNotificationConfigService((config, event, attemptNumber) -> {
            PushDeliveryAttempt out = new PushDeliveryAttempt();
            out.setConfigId(config.getConfigId());
            out.setEndpointUrl(config.getEndpointUrl());
            out.setAttemptNumber(attemptNumber);
            out.setSuccess(true);
            out.setStatusCode(200);
            return out;
        });
        eventService.addListener(pushService::onTaskEvent);
        InMemoryA2ATaskService taskService = new InMemoryA2ATaskService(eventService);

        CreatePushNotificationConfigRequest create = new CreatePushNotificationConfigRequest();
        create.setTaskId("task-diagnostics");
        create.setEndpointUrl("https://example.test/hook");
        create.setRetryBackoffMs(0L);
        pushService.create(create);

        SendMessageRequest send = new SendMessageRequest();
        Part part = new Part();
        part.setPartId("p1");
        part.setType("text");
        part.setText("hello");
        Message message = new Message();
        message.setMessageId("m1");
        message.setRole("user");
        message.setParts(List.of(part));
        send.setMessage(message);
        Task task = taskService.sendMessage(send);
        taskService.transitionTask(task.getTaskId(), TaskState.WAITING, "wait");

        A2ADiagnosticsProcessor processor = new A2ADiagnosticsProcessor(taskService, eventService, pushService);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        processor.process(exchange);

        JsonNode json = mapper.readTree(exchange.getMessage().getBody(String.class));
        assertEquals("UP", json.get("status").asText());
        assertTrue(json.get("tasks").get("total").asInt() >= 1);
        assertEquals(1, json.get("pushNotifications").get("configs").asInt());
        assertTrue(json.get("supportedMethods").isArray());
        assertEquals("application/json", exchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
    }
}
