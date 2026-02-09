package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigResponse;
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
        boolean deleted = configService.delete(request);

        DeletePushNotificationConfigResponse response = new DeletePushNotificationConfigResponse();
        response.setConfigId(request.getConfigId());
        response.setDeleted(deleted);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
