package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.dto.GetTaskRequest;
import io.dscope.camel.a2a.model.dto.GetTaskResponse;
import io.dscope.camel.a2a.service.A2ATaskService;

/**
 * Handles GetTask method invocation.
 */
public class GetTaskProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;

    public GetTaskProcessor(A2ATaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("GetTask requires params object");
        }
        GetTaskRequest request = mapper.convertValue(params, GetTaskRequest.class);
        if (request.getTaskId() == null || request.getTaskId().isBlank()) {
            throw new A2AInvalidParamsException("GetTask requires taskId");
        }

        Task task = taskService.getTask(request.getTaskId());
        GetTaskResponse response = new GetTaskResponse();
        response.setTask(task);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
