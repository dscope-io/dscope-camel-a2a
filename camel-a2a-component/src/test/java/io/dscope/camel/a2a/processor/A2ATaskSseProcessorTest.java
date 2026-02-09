package io.dscope.camel.a2a.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.dto.SendStreamingMessageResponse;
import io.dscope.camel.a2a.service.InMemoryA2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2ATaskSseProcessorTest {

    @Test
    void processRendersOrderedSseEventsAndTerminalClose() throws Exception {
        InMemoryTaskEventService eventService = new InMemoryTaskEventService();
        eventService.publishTaskUpdate(task("task-sse", TaskState.RUNNING, "running"));
        eventService.publishTaskUpdate(task("task-sse", TaskState.COMPLETED, "done"));

        A2ATaskSseProcessor processor = new A2ATaskSseProcessor(eventService);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setHeader("taskId", "task-sse");
        exchange.getIn().setHeader("afterSequence", "0");
        exchange.getIn().setHeader("limit", "50");

        processor.process(exchange);

        String payload = exchange.getMessage().getBody(String.class);
        assertTrue(payload.contains("id: 1"));
        assertTrue(payload.contains("id: 2"));
        assertTrue(payload.contains("event: task.status"));
        assertTrue(payload.contains("event: close"));
        assertEquals("text/event-stream", exchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
    }

    @Test
    void processHandlesStreamingSubscriptionAndCleansTerminalSubscriber() throws Exception {
        InMemoryTaskEventService eventService = new InMemoryTaskEventService();
        InMemoryA2ATaskService taskService = new InMemoryA2ATaskService(eventService);
        SendStreamingMessageProcessor streamingProcessor = new SendStreamingMessageProcessor(taskService, eventService);

        Exchange streamingExchange = new DefaultExchange(new DefaultCamelContext());
        streamingExchange.setProperty(A2AExchangeProperties.NORMALIZED_PARAMS, Map.of(
            "message", Map.of(
                "messageId", "stream-sse-1",
                "role", "user",
                "parts", List.of(Map.of("partId", "p1", "type", "text", "text", "hello stream"))
            )
        ));
        streamingProcessor.process(streamingExchange);

        SendStreamingMessageResponse response =
            streamingExchange.getProperty(A2AExchangeProperties.METHOD_RESULT, SendStreamingMessageResponse.class);
        assertNotNull(response);
        assertNotNull(eventService.getSubscription(response.getSubscriptionId()));

        A2ATaskSseProcessor processor = new A2ATaskSseProcessor(eventService);
        Exchange sseExchange = new DefaultExchange(new DefaultCamelContext());
        sseExchange.getIn().setHeader("taskId", response.getTask().getTaskId());
        sseExchange.getIn().setHeader("subscriptionId", response.getSubscriptionId());
        sseExchange.getIn().setHeader("afterSequence", "0");
        sseExchange.getIn().setHeader("limit", "100");

        processor.process(sseExchange);

        String payload = sseExchange.getMessage().getBody(String.class);
        assertTrue(payload.contains("event: task.status"));
        assertTrue(payload.contains("event: close"));
        assertEquals("text/event-stream", sseExchange.getMessage().getHeader(Exchange.CONTENT_TYPE));
        assertNull(eventService.getSubscription(response.getSubscriptionId()));
    }

    private Task task(String taskId, TaskState state, String message) {
        Task task = new Task();
        task.setTaskId(taskId);
        TaskStatus status = new TaskStatus();
        status.setState(state);
        status.setMessage(message);
        status.setUpdatedAt("2026-02-09T00:00:00Z");
        task.setStatus(status);
        return task;
    }
}
