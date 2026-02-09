package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.CancelTaskResponse;
import io.dscope.camel.a2a.service.A2ATaskService;

/**
 * Handles CancelTask method invocation.
 */
public class CancelTaskProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;

    public CancelTaskProcessor(A2ATaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("CancelTask requires params object");
        }
        CancelTaskRequest request = mapper.convertValue(params, CancelTaskRequest.class);
        if (request.getTaskId() == null || request.getTaskId().isBlank()) {
            throw new A2AInvalidParamsException("CancelTask requires taskId");
        }

        Task task = taskService.cancelTask(request);
        CancelTaskResponse response = new CancelTaskResponse();
        response.setTask(task);
        response.setCanceled(true);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
