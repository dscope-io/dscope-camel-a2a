package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.dto.GetPushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.GetPushNotificationConfigResponse;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;

/**
 * Handles GetPushNotificationConfig method invocation.
 */
public class GetPushNotificationConfigProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2APushNotificationConfigService configService;

    public GetPushNotificationConfigProcessor(A2APushNotificationConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        if (params == null) {
            throw new A2AInvalidParamsException("GetPushNotificationConfig requires params object");
        }
        GetPushNotificationConfigRequest request = mapper.convertValue(params, GetPushNotificationConfigRequest.class);
        PushNotificationConfig config = configService.get(request);

        GetPushNotificationConfigResponse response = new GetPushNotificationConfigResponse();
        response.setConfig(config);
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
