package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsResponse;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;

/**
 * Handles ListPushNotificationConfigs method invocation.
 */
public class ListPushNotificationConfigsProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2APushNotificationConfigService configService;

    public ListPushNotificationConfigsProcessor(A2APushNotificationConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void process(Exchange exchange) {
        Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
        ListPushNotificationConfigsRequest request =
            params == null ? new ListPushNotificationConfigsRequest() : mapper.convertValue(params, ListPushNotificationConfigsRequest.class);

        ListPushNotificationConfigsResponse response = new ListPushNotificationConfigsResponse();
        response.setConfigs(configService.list(request));
        exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
    }
}
