package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsResponse;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;
import java.util.List;

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

        int pageSize = request.getLimit() == null ? 100 : request.getLimit();
        if (pageSize < 1 || pageSize > 100) {
            throw new A2AInvalidParamsException("ListPushNotificationConfigs pageSize must be between 1 and 100");
        }
        int offset = parsePageToken(request.getPageToken());
        request.setLimit(pageSize + 1);
        ListPushNotificationConfigsResponse response = new ListPushNotificationConfigsResponse();
        List<PushNotificationConfig> configs = configService.list(request);
        boolean hasMore = configs.size() > pageSize;
        response.setConfigs(hasMore ? configs.subList(0, pageSize) : configs);
        response.setNextPageToken(hasMore ? Integer.toString(offset + pageSize) : "");
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
            throw new A2AInvalidParamsException("ListPushNotificationConfigs pageToken is invalid");
        }
    }
}
