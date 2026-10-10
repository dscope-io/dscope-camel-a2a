package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.TaskStatusUpdateEvent;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.model.dto.SendStreamingMessageRequest;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.List;
import java.util.Map;

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
        sendRequest.setConfiguration(request.getConfiguration());
        sendRequest.setTenant(request.getTenant());

        Task task = taskService.sendMessage(sendRequest);
        taskService.transitionTask(task.getTaskId(), TaskState.WAITING, "Streaming updates started");
        taskService.transitionTask(task.getTaskId(), TaskState.RUNNING, "Streaming updates in progress");
        taskService.transitionTask(task.getTaskId(), TaskState.COMPLETED, "Streaming updates completed");
        task = taskService.getTask(task.getTaskId());
        String contextId = task.getContextId();
        List<Map<String, Object>> streamResponses = eventService.readTaskEvents(task.getTaskId(), 0L, 100).stream()
            .map(event -> Map.<String, Object>of("statusUpdate", toStatusUpdate(event, contextId)))
            .toList();
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, streamResponses);
    }

    private TaskStatusUpdateEvent toStatusUpdate(TaskEvent event, String contextId) {
        TaskStatus status = new TaskStatus();
        status.setState(event.getState());
        status.setMessage(event.getMessage());
        status.setUpdatedAt(event.getTimestamp());

        TaskStatusUpdateEvent update = new TaskStatusUpdateEvent();
        update.setTaskId(event.getTaskId());
        update.setContextId(contextId);
        update.setStatus(status);
        return update;
    }
}
