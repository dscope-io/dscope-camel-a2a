package io.dscope.camel.a2a.service;

import java.util.logging.Logger;
import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;

/**
 * Default observer that logs push delivery lifecycle events.
 */
public class LoggingPushNotificationObserver implements PushNotificationObserver {

    private static final Logger LOG = Logger.getLogger(LoggingPushNotificationObserver.class.getName());

    @Override
    public void onAttempt(PushNotificationConfig config, TaskEvent event, int attemptNumber) {
        LOG.fine(() -> "Push attempt " + attemptNumber + " for config " + config.getConfigId()
            + " task " + event.getTaskId());
    }

    @Override
    public void onSuccess(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt) {
        LOG.fine(() -> "Push success config " + config.getConfigId()
            + " status " + attempt.getStatusCode());
    }

    @Override
    public void onFailure(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt, boolean willRetry) {
        LOG.fine(() -> "Push failure config " + config.getConfigId()
            + " attempt " + attempt.getAttemptNumber()
            + " willRetry=" + willRetry
            + " error=" + attempt.getErrorMessage());
    }
}
