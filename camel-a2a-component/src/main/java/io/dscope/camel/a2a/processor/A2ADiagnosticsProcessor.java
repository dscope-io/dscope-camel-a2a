package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import io.dscope.camel.a2a.model.PushDeliveryStats;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.service.A2APushNotificationConfigService;
import io.dscope.camel.a2a.service.A2ATaskService;
import io.dscope.camel.a2a.service.InMemoryTaskEventService;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Emits service diagnostics for the sample HTTP endpoints.
 */
public class A2ADiagnosticsProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final A2ATaskService taskService;
    private final InMemoryTaskEventService taskEventService;
    private final A2APushNotificationConfigService pushConfigService;

    public A2ADiagnosticsProcessor(A2ATaskService taskService,
                                   InMemoryTaskEventService taskEventService,
                                   A2APushNotificationConfigService pushConfigService) {
        this.taskService = taskService;
        this.taskEventService = taskEventService;
        this.pushConfigService = pushConfigService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        int taskCount = taskService.listTasks(new ListTasksRequest()).size();
        int subscriptionCount = taskEventService.getActiveSubscriptionCount();
        int eventBacklogCount = taskEventService.getBufferedEventCount();
        int pushConfigCount = pushConfigService.list(new ListPushNotificationConfigsRequest()).size();
        PushDeliveryStats pushStats = pushConfigService.getDeliveryStats();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("timestamp", Instant.now().toString());
        payload.put("tasks", Map.of("total", taskCount));
        payload.put("streaming", Map.of(
            "activeSubscriptions", subscriptionCount,
            "bufferedEvents", eventBacklogCount
        ));
        payload.put("pushNotifications", Map.of(
            "configs", pushConfigCount,
            "attempts", pushStats.getAttempts(),
            "successes", pushStats.getSuccesses(),
            "failures", pushStats.getFailures()
        ));
        payload.put("supportedMethods", List.copyOf(new TreeSet<>(A2AProtocolMethods.CORE_METHODS)));

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(mapper.writeValueAsString(payload));
    }
}
