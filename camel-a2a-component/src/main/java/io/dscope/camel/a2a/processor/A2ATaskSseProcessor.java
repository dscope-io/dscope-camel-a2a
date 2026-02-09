package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.List;
import java.util.Map;

/**
 * Renders task events as an SSE payload.
 */
public class A2ATaskSseProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final InMemoryTaskEventService eventService;

    public A2ATaskSseProcessor(InMemoryTaskEventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        String taskId = resolveTaskId(in);
        if (taskId == null || taskId.isBlank()) {
            throw new A2AInvalidParamsException("SSE requires taskId path parameter");
        }

        String subscriptionId = stringValue(in.getHeader("subscriptionId"));
        long afterSequence = parseLong(in.getHeader("afterSequence"), 0L);
        int limit = (int) parseLong(in.getHeader("limit"), 100L);

        List<TaskEvent> events = eventService.readTaskEvents(taskId, afterSequence, limit);
        long lastSequence = afterSequence;
        StringBuilder payload = new StringBuilder();
        for (TaskEvent event : events) {
            payload.append("id: ").append(event.getSequence()).append('\n');
            payload.append("event: ").append(event.getEventType() == null ? "task.status" : event.getEventType()).append('\n');
            payload.append("data: ").append(mapper.writeValueAsString(event)).append("\n\n");
            lastSequence = event.getSequence();
        }

        boolean terminal = events.stream().anyMatch(TaskEvent::isTerminal) || eventService.isTaskTerminal(taskId);
        if (events.isEmpty() && terminal) {
            payload.append("event: complete\n");
            payload.append("data: ").append(mapper.writeValueAsString(Map.of("taskId", taskId, "terminal", true))).append("\n\n");
        }

        if (terminal) {
            payload.append("event: close\n");
            payload.append("data: ").append(mapper.writeValueAsString(Map.of("taskId", taskId, "reason", "terminal"))).append("\n\n");
        }

        eventService.acknowledgeSubscription(subscriptionId, lastSequence, terminal);
        if (terminal) {
            eventService.cleanupTerminalSubscriptions();
        }

        Message out = exchange.getMessage();
        out.setHeader(Exchange.CONTENT_TYPE, "text/event-stream");
        out.setHeader("Cache-Control", "no-cache");
        out.setHeader("Connection", "keep-alive");
        out.setBody(payload.toString());
    }

    private String resolveTaskId(Message message) {
        String taskId = stringValue(message.getHeader("taskId"));
        if (taskId != null) {
            return taskId;
        }
        String httpPath = stringValue(message.getHeader(Exchange.HTTP_PATH));
        if (httpPath == null || httpPath.isBlank()) {
            return null;
        }
        int slash = httpPath.lastIndexOf('/');
        return slash >= 0 ? httpPath.substring(slash + 1) : httpPath;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private long parseLong(Object value, long fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
