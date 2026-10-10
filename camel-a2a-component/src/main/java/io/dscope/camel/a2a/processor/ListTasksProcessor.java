package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.ListTasksResponse;
import io.dscope.camel.a2a.service.A2ATaskService;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles ListTasks method invocation.
 */
public class ListTasksProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;

    public ListTasksProcessor(A2ATaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        ListTasksRequest request = params == null ? new ListTasksRequest() : mapper.convertValue(params, ListTasksRequest.class);
        int pageSize = request.getLimit() == null ? 50 : request.getLimit();
        if (pageSize < 1 || pageSize > 100) {
            throw new A2AInvalidParamsException("ListTasks pageSize must be between 1 and 100");
        }
        if (request.getHistoryLength() != null && request.getHistoryLength() < 0) {
            throw new A2AInvalidParamsException("ListTasks historyLength cannot be negative");
        }
        int offset = parsePageToken(request.getCursor());
        Instant timestampAfter = parseStatusTimestampAfter(request.getStatusTimestampAfter());

        ListTasksRequest query = new ListTasksRequest();
        query.setState(request.getState());
        query.setContextId(request.getContextId());
        List<Task> matchingTasks = taskService.listTasks(query).stream()
            .filter(task -> request.getContextId() == null || request.getContextId().equals(task.getContextId()))
            .filter(task -> matchesStatusTimestamp(task, timestampAfter))
            .toList();
        if (offset > matchingTasks.size()) {
            throw new A2AInvalidParamsException("ListTasks pageToken is out of range");
        }
        int end = Math.min(matchingTasks.size(), offset + pageSize);
        List<Task> tasks = new ArrayList<>(matchingTasks.subList(offset, end));
        tasks.replaceAll(task -> projectTask(task, request));

        ListTasksResponse response = new ListTasksResponse();
        response.setTasks(tasks);
        response.setNextCursor(end < matchingTasks.size() ? Integer.toString(end) : "");
        response.setPageSize(pageSize);
        response.setTotalSize(matchingTasks.size());
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }

    private int parsePageToken(String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return 0;
        }
        try {
            int offset = Integer.parseInt(pageToken);
            if (offset < 0) {
                throw new NumberFormatException();
            }
            return offset;
        } catch (NumberFormatException e) {
            throw new A2AInvalidParamsException("ListTasks pageToken is invalid");
        }
    }

    private Instant parseStatusTimestampAfter(String timestampAfter) {
        if (timestampAfter == null || timestampAfter.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(timestampAfter);
        } catch (DateTimeParseException e) {
            throw new A2AInvalidParamsException("ListTasks statusTimestampAfter must be an ISO 8601 timestamp");
        }
    }

    private boolean matchesStatusTimestamp(Task task, Instant timestampAfter) {
        if (timestampAfter == null) {
            return true;
        }
        return task.getStatus() != null && task.getStatus().getUpdatedAt() != null
            && !Instant.parse(task.getStatus().getUpdatedAt()).isBefore(timestampAfter);
    }

    private Task projectTask(Task task, ListTasksRequest request) {
        Task projected = mapper.convertValue(task, Task.class);
        if (!Boolean.TRUE.equals(request.getIncludeArtifacts())) {
            projected.setArtifacts(List.of());
        }
        if (request.getHistoryLength() != null) {
            List<Message> history = projected.getMessages();
            if (history == null || request.getHistoryLength() == 0) {
                projected.setMessages(List.of());
            } else if (history.size() > request.getHistoryLength()) {
                projected.setMessages(history.subList(history.size() - request.getHistoryLength(), history.size()));
            }
        }
        return projected;
    }
}
