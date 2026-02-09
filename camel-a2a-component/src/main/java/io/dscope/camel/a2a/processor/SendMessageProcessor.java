package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.model.dto.SendMessageResponse;
import io.dscope.camel.a2a.service.A2ATaskService;

/**
 * Handles SendMessage method invocation.
 */
public class SendMessageProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;

    public SendMessageProcessor(A2ATaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("SendMessage requires params object");
        }
        SendMessageRequest request = mapper.convertValue(params, SendMessageRequest.class);
        if (request.getMessage() == null) {
            throw new A2AInvalidParamsException("SendMessage requires message");
        }

        Task task = taskService.sendMessage(request);
        SendMessageResponse response = new SendMessageResponse();
        response.setTask(task);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
