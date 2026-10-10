package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigRequest;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;

/**
 * Handles DeletePushNotificationConfig method invocation.
 */
public class DeletePushNotificationConfigProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2APushNotificationConfigService configService;

    public DeletePushNotificationConfigProcessor(A2APushNotificationConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("DeletePushNotificationConfig requires params object");
        }
        DeletePushNotificationConfigRequest request = mapper.convertValue(params, DeletePushNotificationConfigRequest.class);
        if (A2AProtocolMethods.DELETE_TASK_PUSH_NOTIFICATION_CONFIG.equals(
            exchange.getProperty(A2AExchangeProperties.METHOD, String.class))) {
            if (request.getTaskId() == null || request.getTaskId().isBlank()) {
                throw new A2AInvalidParamsException("DeleteTaskPushNotificationConfig requires taskId");
            }
        } else {
            request.setTaskId(null);
        }
        boolean deleted = configService.delete(request);
        if (!deleted) {
            throw new A2AInvalidParamsException("Push config not found: " + request.getConfigId());
        }

        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, java.util.Map.of());
    }
}
