package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.PushDeliveryAttempt;
import io.dscope.camel.a2a.model.PushDeliveryStats;
import io.dscope.camel.a2a.model.PushNotificationConfig;
import io.dscope.camel.a2a.model.TaskEvent;
import io.dscope.camel.a2a.model.dto.CreatePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.DeletePushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.GetPushNotificationConfigRequest;
import io.dscope.camel.a2a.model.dto.ListPushNotificationConfigsRequest;
import io.dscope.camel.a2a.processor.A2AInvalidParamsException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory push notification config service with retry and observer hooks.
 */
public class InMemoryPushNotificationConfigService implements A2APushNotificationConfigService {

    private final ConcurrentMap<String, PushNotificationConfig> configsById = new ConcurrentHashMap<>();
    private final PushNotificationNotifier notifier;
    private final List<PushNotificationObserver> observers;
    private final AtomicLong attempts = new AtomicLong();
    private final AtomicLong successes = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private final int maxRetryCap;
    private final long maxBackoffMs;

    public InMemoryPushNotificationConfigService(PushNotificationNotifier notifier) {
        this(notifier, List.of(new LoggingPushNotificationObserver()), 8, 1000L);
    }

    public InMemoryPushNotificationConfigService(PushNotificationNotifier notifier,
                                                 List<PushNotificationObserver> observers,
                                                 int maxRetryCap,
                                                 long maxBackoffMs) {
        this.notifier = notifier;
        this.observers = observers == null ? List.of() : List.copyOf(observers);
        this.maxRetryCap = Math.max(0, maxRetryCap);
        this.maxBackoffMs = Math.max(0L, maxBackoffMs);
    }

    @Override
    public PushNotificationConfig create(CreatePushNotificationConfigRequest request) {
        if (request == null) {
            throw new A2AInvalidParamsException("CreatePushNotificationConfig requires params object");
        }
        if (request.getEndpointUrl() == null || request.getEndpointUrl().isBlank()) {
            throw new A2AInvalidParamsException("CreatePushNotificationConfig requires endpointUrl");
        }

        String now = Instant.now().toString();
        PushNotificationConfig config = new PushNotificationConfig();
        config.setConfigId(UUID.randomUUID().toString());
        config.setTaskId(normalizeBlank(request.getTaskId()));
        config.setEndpointUrl(request.getEndpointUrl());
        config.setSecret(request.getSecret());
        config.setHeaders(request.getHeaders());
        config.setMetadata(request.getMetadata());
        config.setEnabled(request.getEnabled() == null || request.getEnabled());
        config.setMaxRetries(normalizeRetries(request.getMaxRetries()));
        config.setRetryBackoffMs(normalizeBackoff(request.getRetryBackoffMs()));
        config.setCreatedAt(now);
        config.setUpdatedAt(now);

        configsById.put(config.getConfigId(), config);
        return copyConfig(config);
    }

    @Override
    public PushNotificationConfig get(GetPushNotificationConfigRequest request) {
        if (request == null || request.getConfigId() == null || request.getConfigId().isBlank()) {
            throw new A2AInvalidParamsException("GetPushNotificationConfig requires configId");
        }
        PushNotificationConfig config = configsById.get(request.getConfigId());
        if (config == null) {
            throw new A2AInvalidParamsException("Push config not found: " + request.getConfigId());
        }
        return copyConfig(config);
    }

    @Override
    public List<PushNotificationConfig> list(ListPushNotificationConfigsRequest request) {
        int limit = 100;
        String taskId = null;
        if (request != null) {
            if (request.getLimit() != null) {
                if (request.getLimit() <= 0) {
                    throw new A2AInvalidParamsException("ListPushNotificationConfigs limit must be greater than zero");
                }
                limit = request.getLimit();
            }
            taskId = normalizeBlank(request.getTaskId());
        }
        String finalTaskId = taskId;
        return configsById.values().stream()
            .filter(c -> finalTaskId == null || finalTaskId.equals(c.getTaskId()))
            .sorted(Comparator.comparing(PushNotificationConfig::getCreatedAt))
            .limit(limit)
            .map(this::copyConfig)
            .collect(Collectors.toList());
    }

    @Override
    public boolean delete(DeletePushNotificationConfigRequest request) {
        if (request == null || request.getConfigId() == null || request.getConfigId().isBlank()) {
            throw new A2AInvalidParamsException("DeletePushNotificationConfig requires configId");
        }
        return configsById.remove(request.getConfigId()) != null;
    }

    @Override
    public void onTaskEvent(TaskEvent event) {
        if (event == null || event.getTaskId() == null || event.getTaskId().isBlank()) {
            return;
        }

        List<PushNotificationConfig> matching = new ArrayList<>();
        for (PushNotificationConfig config : configsById.values()) {
            if (!config.isEnabled()) {
                continue;
            }
            if (config.getTaskId() == null || config.getTaskId().equals(event.getTaskId())) {
                matching.add(config);
            }
        }

        for (PushNotificationConfig config : matching) {
            int maxAttempts = Math.min(config.getMaxRetries(), maxRetryCap) + 1;
            long backoff = Math.min(config.getRetryBackoffMs(), maxBackoffMs);
            for (int attemptNumber = 1; attemptNumber <= maxAttempts; attemptNumber++) {
                final int currentAttempt = attemptNumber;
                observers.forEach(o -> o.onAttempt(config, event, currentAttempt));
                attempts.incrementAndGet();
                PushDeliveryAttempt attempt = notifier.notify(config, event, currentAttempt);
                if (attempt.isSuccess()) {
                    successes.incrementAndGet();
                    observers.forEach(o -> o.onSuccess(config, event, attempt));
                    break;
                }
                failures.incrementAndGet();
                boolean willRetry = attemptNumber < maxAttempts;
                observers.forEach(o -> o.onFailure(config, event, attempt, willRetry));
                if (willRetry && backoff > 0L) {
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public PushDeliveryStats getDeliveryStats() {
        PushDeliveryStats stats = new PushDeliveryStats();
        stats.setAttempts(attempts.get());
        stats.setSuccesses(successes.get());
        stats.setFailures(failures.get());
        return stats;
    }

    private int normalizeRetries(Integer retries) {
        if (retries == null) {
            return 3;
        }
        if (retries < 0) {
            throw new A2AInvalidParamsException("maxRetries must be >= 0");
        }
        return retries;
    }

    private long normalizeBackoff(Long backoffMs) {
        if (backoffMs == null) {
            return 100L;
        }
        if (backoffMs < 0) {
            throw new A2AInvalidParamsException("retryBackoffMs must be >= 0");
        }
        return backoffMs;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private PushNotificationConfig copyConfig(PushNotificationConfig source) {
        PushNotificationConfig copy = new PushNotificationConfig();
        copy.setConfigId(source.getConfigId());
        copy.setTaskId(source.getTaskId());
        copy.setEndpointUrl(source.getEndpointUrl());
        copy.setSecret(source.getSecret());
        copy.setEnabled(source.isEnabled());
        copy.setMaxRetries(source.getMaxRetries());
        copy.setRetryBackoffMs(source.getRetryBackoffMs());
        copy.setHeaders(source.getHeaders());
        copy.setMetadata(source.getMetadata());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }
}
