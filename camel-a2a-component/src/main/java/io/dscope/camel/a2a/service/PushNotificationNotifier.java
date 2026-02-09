package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;

/**
 * Sends task events to push notification endpoints.
 */
public interface PushNotificationNotifier {

    PushDeliveryAttempt notify(PushNotificationConfig config, TaskEvent event, int attemptNumber);
}
