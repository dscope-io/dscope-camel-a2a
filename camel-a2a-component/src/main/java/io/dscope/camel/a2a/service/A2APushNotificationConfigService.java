package io.dscope.camel.a2a.service;

import java.util.List;
import io.dscope.camel.a2a.model.PushDeliveryStats;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.GetPushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;

/**
 * CRUD operations for push notification config and event delivery handling.
 */
public interface A2APushNotificationConfigService {

    PushNotificationConfig create(CreatePushNotificationConfigRequest request);

    PushNotificationConfig get(GetPushNotificationConfigRequest request);

    List<PushNotificationConfig> list(ListPushNotificationConfigsRequest request);

    boolean delete(DeletePushNotificationConfigRequest request);

    void onTaskEvent(TaskEvent event);

    PushDeliveryStats getDeliveryStats();
}
