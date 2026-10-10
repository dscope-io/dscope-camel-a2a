package io.dscope.camel.a2a.config;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Method names supported by the current protocol layer.
 */
public final class A2AProtocolMethods {

    public static final String SEND_MESSAGE = "SendMessage";
    public static final String SEND_STREAMING_MESSAGE = "SendStreamingMessage";
    public static final String GET_TASK = "GetTask";
    public static final String LIST_TASKS = "ListTasks";
    public static final String CANCEL_TASK = "CancelTask";
    public static final String SUBSCRIBE_TO_TASK = "SubscribeToTask";
    public static final String CREATE_TASK_PUSH_NOTIFICATION_CONFIG = "CreateTaskPushNotificationConfig";
    public static final String GET_TASK_PUSH_NOTIFICATION_CONFIG = "GetTaskPushNotificationConfig";
    public static final String LIST_TASK_PUSH_NOTIFICATION_CONFIGS = "ListTaskPushNotificationConfigs";
    public static final String DELETE_TASK_PUSH_NOTIFICATION_CONFIG = "DeleteTaskPushNotificationConfig";
    public static final String CREATE_PUSH_NOTIFICATION_CONFIG = "CreatePushNotificationConfig";
    public static final String GET_PUSH_NOTIFICATION_CONFIG = "GetPushNotificationConfig";
    public static final String LIST_PUSH_NOTIFICATION_CONFIGS = "ListPushNotificationConfigs";
    public static final String DELETE_PUSH_NOTIFICATION_CONFIG = "DeletePushNotificationConfig";
    public static final String GET_EXTENDED_AGENT_CARD = "GetExtendedAgentCard";
    public static final String LEGACY_INTENT_EXECUTE = "intent/execute";

    public static final Set<String> CORE_METHODS = Set.of(
        SEND_MESSAGE,
        SEND_STREAMING_MESSAGE,
        GET_TASK,
        LIST_TASKS,
        CANCEL_TASK,
        SUBSCRIBE_TO_TASK,
        CREATE_TASK_PUSH_NOTIFICATION_CONFIG,
        GET_TASK_PUSH_NOTIFICATION_CONFIG,
        LIST_TASK_PUSH_NOTIFICATION_CONFIGS,
        DELETE_TASK_PUSH_NOTIFICATION_CONFIG,
        GET_EXTENDED_AGENT_CARD
    );

    public static final Set<String> LEGACY_METHODS = Set.of(
        CREATE_PUSH_NOTIFICATION_CONFIG,
        GET_PUSH_NOTIFICATION_CONFIG,
        LIST_PUSH_NOTIFICATION_CONFIGS,
        DELETE_PUSH_NOTIFICATION_CONFIG,
        LEGACY_INTENT_EXECUTE
    );

    public static final Set<String> SUPPORTED_METHODS = Stream.concat(CORE_METHODS.stream(), LEGACY_METHODS.stream())
        .collect(Collectors.toUnmodifiableSet());

    private A2AProtocolMethods() {
    }
}
