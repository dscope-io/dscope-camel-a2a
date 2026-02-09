package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.ListTasksResponse;
import io.dscope.camel.a2a.service.A2ATaskService;

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
        if (request.getLimit() != null && request.getLimit() <= 0) {
            throw new A2AInvalidParamsException("ListTasks limit must be greater than zero");
        }

        ListTasksResponse response = new ListTasksResponse();
        response.setTasks(taskService.listTasks(request));
        response.setNextCursor(null);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
