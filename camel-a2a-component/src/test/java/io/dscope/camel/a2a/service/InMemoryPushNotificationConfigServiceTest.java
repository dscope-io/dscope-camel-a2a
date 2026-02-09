package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.GetPushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPushNotificationConfigServiceTest {

    @Test
    void createGetListDeleteFlowWorks() {
        InMemoryPushNotificationConfigService service = new InMemoryPushNotificationConfigService(successNotifier());

        CreatePushNotificationConfigRequest create = new CreatePushNotificationConfigRequest();
        create.setTaskId("task-1");
        create.setEndpointUrl("https://hooks.example/push");
        create.setMaxRetries(2);
        create.setRetryBackoffMs(0L);

        PushNotificationConfig created = service.create(create);
        assertNotNull(created.getConfigId());

        GetPushNotificationConfigRequest get = new GetPushNotificationConfigRequest();
        get.setConfigId(created.getConfigId());
        PushNotificationConfig fetched = service.get(get);
        assertEquals(created.getConfigId(), fetched.getConfigId());

        ListPushNotificationConfigsRequest list = new ListPushNotificationConfigsRequest();
        list.setTaskId("task-1");
        list.setLimit(10);
        List<PushNotificationConfig> configs = service.list(list);
        assertEquals(1, configs.size());

        DeletePushNotificationConfigRequest delete = new DeletePushNotificationConfigRequest();
        delete.setConfigId(created.getConfigId());
        assertTrue(service.delete(delete));
        assertFalse(service.delete(delete));
    }

    @Test
    void notifyTaskEventUsesBoundedRetryAndCollectsStats() {
        AtomicInteger attempts = new AtomicInteger();
        PushNotificationNotifier notifier = (config, event, attemptNumber) -> {
            PushDeliveryAttempt out = new PushDeliveryAttempt();
            out.setConfigId(config.getConfigId());
            out.setEndpointUrl(config.getEndpointUrl());
            out.setAttemptNumber(attemptNumber);
            int current = attempts.incrementAndGet();
            if (current < 3) {
                out.setSuccess(false);
                out.setStatusCode(500);
                out.setErrorMessage("temporary failure");
            } else {
                out.setSuccess(true);
                out.setStatusCode(202);
            }
            return out;
        };

        InMemoryPushNotificationConfigService service = new InMemoryPushNotificationConfigService(
            notifier,
            List.of(),
            5,
            0L
        );

        CreatePushNotificationConfigRequest create = new CreatePushNotificationConfigRequest();
        create.setTaskId("task-r");
        create.setEndpointUrl("https://hooks.example/retry");
        create.setMaxRetries(4);
        create.setRetryBackoffMs(0L);
        service.create(create);

        service.onTaskEvent(event("task-r"));

        assertEquals(3, attempts.get());
        assertEquals(3, service.getDeliveryStats().getAttempts());
        assertEquals(1, service.getDeliveryStats().getSuccesses());
        assertEquals(2, service.getDeliveryStats().getFailures());
    }

    @Test
    void invalidInputsAreRejected() {
        InMemoryPushNotificationConfigService service = new InMemoryPushNotificationConfigService(successNotifier());

        CreatePushNotificationConfigRequest badCreate = new CreatePushNotificationConfigRequest();
        badCreate.setEndpointUrl(" ");
        A2AInvalidParamsException createError = assertThrows(A2AInvalidParamsException.class, () -> service.create(badCreate));
        assertEquals("CreatePushNotificationConfig requires endpointUrl", createError.getMessage());

        GetPushNotificationConfigRequest badGet = new GetPushNotificationConfigRequest();
        badGet.setConfigId("missing");
        A2AInvalidParamsException getError = assertThrows(A2AInvalidParamsException.class, () -> service.get(badGet));
        assertEquals("Push config not found: missing", getError.getMessage());

        ListPushNotificationConfigsRequest badList = new ListPushNotificationConfigsRequest();
        badList.setLimit(0);
        A2AInvalidParamsException listError = assertThrows(A2AInvalidParamsException.class, () -> service.list(badList));
        assertEquals("ListPushNotificationConfigs limit must be greater than zero", listError.getMessage());

        DeletePushNotificationConfigRequest badDelete = new DeletePushNotificationConfigRequest();
        A2AInvalidParamsException deleteError = assertThrows(A2AInvalidParamsException.class, () -> service.delete(badDelete));
        assertEquals("DeletePushNotificationConfig requires configId", deleteError.getMessage());
    }

    private PushNotificationNotifier successNotifier() {
        return (config, event, attemptNumber) -> {
            PushDeliveryAttempt out = new PushDeliveryAttempt();
            out.setConfigId(config.getConfigId());
            out.setEndpointUrl(config.getEndpointUrl());
            out.setAttemptNumber(attemptNumber);
            out.setSuccess(true);
            out.setStatusCode(200);
            return out;
        };
    }

    private TaskEvent event(String taskId) {
        TaskEvent event = new TaskEvent();
        event.setTaskId(taskId);
        event.setEventType("task.status");
        event.setSequence(1L);
        event.setState(TaskState.RUNNING);
        event.setTimestamp("2026-02-09T00:00:00Z");
        event.setTerminal(false);
        return event;
    }
}
