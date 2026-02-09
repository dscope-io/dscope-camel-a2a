package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;

/**
 * Observability hook for push delivery attempts.
 */
public interface PushNotificationObserver {

    default void onAttempt(PushNotificationConfig config, TaskEvent event, int attemptNumber) {
    }

    default void onSuccess(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt) {
    }

    default void onFailure(PushNotificationConfig config, TaskEvent event, PushDeliveryAttempt attempt, boolean willRetry) {
    }
}
