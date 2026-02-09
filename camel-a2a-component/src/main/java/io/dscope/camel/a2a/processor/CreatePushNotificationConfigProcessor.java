package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigResponse;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;

/**
 * Handles CreatePushNotificationConfig method invocation.
 */
public class CreatePushNotificationConfigProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2APushNotificationConfigService configService;

    public CreatePushNotificationConfigProcessor(A2APushNotificationConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("CreatePushNotificationConfig requires params object");
        }
        CreatePushNotificationConfigRequest request = mapper.convertValue(params, CreatePushNotificationConfigRequest.class);
        PushNotificationConfig created = configService.create(request);

        CreatePushNotificationConfigResponse response = new CreatePushNotificationConfigResponse();
        response.setConfig(created);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
