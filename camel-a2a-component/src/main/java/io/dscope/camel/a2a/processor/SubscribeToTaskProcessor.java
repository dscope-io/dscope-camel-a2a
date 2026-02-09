package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.TaskSubscription;
import io.dscope.camel.a2a.model.dto.SubscribeToTaskRequest;
import io.dscope.camel.a2a.model.dto.SubscribeToTaskResponse;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

/**
 * Handles SubscribeToTask method invocation.
 */
public class SubscribeToTaskProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;
    private final InMemoryTaskEventService eventService;

    public SubscribeToTaskProcessor(A2ATaskService taskService, InMemoryTaskEventService eventService) {
        this.taskService = taskService;
        this.eventService = eventService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("SubscribeToTask requires params object");
        }

        SubscribeToTaskRequest request = mapper.convertValue(params, SubscribeToTaskRequest.class);
        if (request.getTaskId() == null || request.getTaskId().isBlank()) {
            throw new A2AInvalidParamsException("SubscribeToTask requires taskId");
        }
        if (request.getLimit() != null && request.getLimit() <= 0) {
            throw new A2AInvalidParamsException("SubscribeToTask limit must be greater than zero");
        }

        taskService.getTask(request.getTaskId());
        long afterSequence = request.getAfterSequence() == null ? 0L : Math.max(0L, request.getAfterSequence());
        TaskSubscription subscription = eventService.createSubscription(request.getTaskId(), afterSequence);

        SubscribeToTaskResponse response = new SubscribeToTaskResponse();
        response.setSubscriptionId(subscription.getSubscriptionId());
        response.setTaskId(request.getTaskId());
        response.setAfterSequence(afterSequence);
        response.setTerminal(eventService.isTaskTerminal(request.getTaskId()));
        response.setStreamUrl(buildStreamUrl(request.getTaskId(), subscription.getSubscriptionId(), afterSequence, request.getLimit()));
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }

    private String buildStreamUrl(String taskId, String subscriptionId, long afterSequence, Integer limit) {
        String suffix = limit == null ? "" : "&limit=" + limit;
        return "/a2a/sse/" + taskId + "?subscriptionId=" + subscriptionId + "&afterSequence=" + afterSequence + suffix;
    }
}
