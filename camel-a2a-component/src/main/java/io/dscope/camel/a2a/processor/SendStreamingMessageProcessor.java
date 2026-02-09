package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskSubscription;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.model.dto.SendStreamingMessageRequest;
import io.dscope.camel.a2a.model.dto.SendStreamingMessageResponse;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

/**
 * Handles SendStreamingMessage method invocation.
 */
public class SendStreamingMessageProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;
    private final InMemoryTaskEventService eventService;

    public SendStreamingMessageProcessor(A2ATaskService taskService, InMemoryTaskEventService eventService) {
        this.taskService = taskService;
        this.eventService = eventService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("SendStreamingMessage requires params object");
        }
        SendStreamingMessageRequest request = mapper.convertValue(params, SendStreamingMessageRequest.class);
        if (request.getMessage() == null) {
            throw new A2AInvalidParamsException("SendStreamingMessage requires message");
        }

        SendMessageRequest sendRequest = new SendMessageRequest();
        sendRequest.setMessage(request.getMessage());
        sendRequest.setConversationId(request.getConversationId());
        sendRequest.setIdempotencyKey(request.getIdempotencyKey());
        sendRequest.setMetadata(request.getMetadata());

        Task task = taskService.sendMessage(sendRequest);
        taskService.transitionTask(task.getTaskId(), TaskState.WAITING, "Streaming updates started");
        taskService.transitionTask(task.getTaskId(), TaskState.RUNNING, "Streaming updates in progress");
        taskService.transitionTask(task.getTaskId(), TaskState.COMPLETED, "Streaming updates completed");
        task = taskService.getTask(task.getTaskId());

        TaskSubscription subscription = eventService.createSubscription(task.getTaskId(), 0L);
        SendStreamingMessageResponse response = new SendStreamingMessageResponse();
        response.setTask(task);
        response.setSubscriptionId(subscription.getSubscriptionId());
        response.setStreamUrl(buildStreamUrl(task.getTaskId(), subscription.getSubscriptionId(), 0L));
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }

    private String buildStreamUrl(String taskId, String subscriptionId, long afterSequence) {
        return "/a2a/sse/" + taskId + "?subscriptionId=" + subscriptionId + "&afterSequence=" + afterSequence;
    }
}
