package io.dscope.camel.a2a.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Sample webhook notifier implementation using Java HttpClient.
 */
public class WebhookPushNotificationNotifier implements PushNotificationNotifier {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Duration requestTimeout;

    public WebhookPushNotificationNotifier() {
        this(HttpClient.newHttpClient(), new ObjectMapper(), Duration.ofSeconds(3));
    }

    public WebhookPushNotificationNotifier(HttpClient client, ObjectMapper mapper, Duration requestTimeout) {
        this.client = client;
        this.mapper = mapper;
        this.requestTimeout = requestTimeout == null ? Duration.ofSeconds(3) : requestTimeout;
    }

    @Override
    public PushDeliveryAttempt notify(PushNotificationConfig config, TaskEvent event, int attemptNumber) {
        PushDeliveryAttempt attempt = new PushDeliveryAttempt();
        attempt.setConfigId(config.getConfigId());
        attempt.setEndpointUrl(config.getEndpointUrl());
        attempt.setAttemptNumber(attemptNumber);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(config.getEndpointUrl()))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json");

            if (config.getSecret() != null && !config.getSecret().isBlank()) {
                builder.header("X-A2A-Webhook-Secret", config.getSecret());
            }
            if (config.getHeaders() != null) {
                config.getHeaders().forEach(builder::header);
            }

            String payload = mapper.writeValueAsString(Map.of(
                "taskId", event.getTaskId(),
                "sequence", event.getSequence(),
                "eventType", event.getEventType(),
                "state", event.getState() == null ? null : event.getState().name(),
                "message", event.getMessage(),
                "timestamp", event.getTimestamp(),
                "terminal", event.isTerminal(),
                "payload", event.getPayload()
            ));

            HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            attempt.setStatusCode(response.statusCode());
            attempt.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);
            if (!attempt.isSuccess()) {
                attempt.setErrorMessage("HTTP " + response.statusCode());
            }
            return attempt;
        } catch (Exception e) {
            attempt.setSuccess(false);
            attempt.setStatusCode(0);
            attempt.setErrorMessage(e.getMessage());
            return attempt;
        }
    }
}
