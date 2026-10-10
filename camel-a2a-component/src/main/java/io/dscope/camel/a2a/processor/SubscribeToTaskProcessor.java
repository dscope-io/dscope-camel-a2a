package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.TaskStatusUpdateEvent;
import io.dscope.camel.a2a.model.dto.SubscribeToTaskRequest;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.util.List;
import java.util.Map;

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

        var task = taskService.getTask(request.getTaskId());
        TaskState state = task.getStatus() == null ? null : task.getStatus().getState();
        if (state == TaskState.COMPLETED || state == TaskState.FAILED
            || state == TaskState.CANCELED || state == TaskState.REJECTED) {
            throw new A2AUnsupportedOperationException("Cannot subscribe to a task in a terminal state");
        }
        long afterSequence = request.getAfterSequence() == null ? 0L : Math.max(0L, request.getAfterSequence());
        List<Map<String, Object>> streamResponses = eventService
            .readTaskEvents(request.getTaskId(), afterSequence, request.getLimit() == null ? 100 : request.getLimit())
            .stream()
            .map(event -> Map.<String, Object>of("statusUpdate", toStatusUpdate(event, task.getContextId())))
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
