package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default observer that logs push delivery lifecycle events.
 */
public class LoggingPushNotificationObserver implements PushNotificationObserver {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingPushNotificationObserver.class);

    @Override
    public void onAttempt(PushNotificationConfig config, TaskEvent event, int attemptNumber) {
        LOG.debug("Push attempt {} for config {} task {}", attemptNumber, config.getConfigId(), event.getTaskId());
    }

    @Override
    public void onSuccess(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt) {
        LOG.debug("Push success config {} status {}", config.getConfigId(), attempt.getStatusCode());
    }

    @Override
    public void onFailure(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt, boolean willRetry) {
        LOG.debug("Push failure config {} attempt {} willRetry={} error={}",
            config.getConfigId(),
            attempt.getAttemptNumber(),
            willRetry,
            attempt.getErrorMessage());
    }
}
