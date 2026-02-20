package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Message;
import io.dscope.camel.a2a.model.Part;
import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.persistence.core.RehydrationPolicy;
import io.dscope.camel.persistence.redis.RedisFlowStateStore;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentA2APersistenceRedisTest {

    @Test
    void taskRoundTripAcrossServiceInstances() {
        String uri = redisUri();
        Assumptions.assumeTrue(isRedisReachable(uri), "Redis not reachable at " + uri);

        RedisFlowStateStore store = newRedisStore(uri);

        PersistentA2ATaskEventService eventsFirst = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksFirst = new PersistentA2ATaskService(store, eventsFirst, RehydrationPolicy.DEFAULT);

        Task created = tasksFirst.sendMessage(newSendMessageRequest("redis-msg-1"));

        PersistentA2ATaskEventService eventsSecond = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksSecond = new PersistentA2ATaskService(store, eventsSecond, RehydrationPolicy.DEFAULT);

        Task restored = tasksSecond.getTask(created.getTaskId());
        assertEquals(created.getTaskId(), restored.getTaskId());
        assertEquals(TaskState.RUNNING, restored.getStatus().getState());

        List<?> restoredEvents = eventsSecond.readTaskEvents(created.getTaskId(), 0L, 20);
        assertNotNull(restoredEvents);
    }

    @Test
    void taskContinuationWorksAfterSnapshotRehydrationAcrossInstances() {
        String uri = redisUri();
        Assumptions.assumeTrue(isRedisReachable(uri), "Redis not reachable at " + uri);

        RedisFlowStateStore store = newRedisStore(uri);
        RehydrationPolicy aggressiveSnapshots = new RehydrationPolicy(1, 500, 200);

        PersistentA2ATaskEventService eventsFirst = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksFirst = new PersistentA2ATaskService(store, eventsFirst, aggressiveSnapshots);
        Task created = tasksFirst.sendMessage(newSendMessageRequest("redis-msg-2"));

        PersistentA2ATaskEventService eventsSecond = new PersistentA2ATaskEventService(store);
        PersistentA2ATaskService tasksSecond = new PersistentA2ATaskService(store, eventsSecond, aggressiveSnapshots);
        Task completed = tasksSecond.transitionTask(created.getTaskId(), TaskState.COMPLETED, "done-redis");
        assertEquals(TaskState.COMPLETED, completed.getStatus().getState());

        PersistentA2ATaskService tasksThird = new PersistentA2ATaskService(store, new PersistentA2ATaskEventService(store), aggressiveSnapshots);
        Task restored = tasksThird.getTask(created.getTaskId());
        assertEquals(TaskState.COMPLETED, restored.getStatus().getState());

        assertTrue(store.rehydrate("a2a.task", created.getTaskId()).envelope().snapshot().has("task"));
    }

    private RedisFlowStateStore newRedisStore(String uri) {
        String prefix = "camel:a2a:test:" + UUID.randomUUID().toString().replace("-", "");
        return new RedisFlowStateStore(uri, prefix);
    }

    private String redisUri() {
        return System.getProperty("camel.persistence.test.redis.uri", "redis://localhost:6379");
    }

    private boolean isRedisReachable(String uri) {
        try (JedisPool pool = new JedisPool(uri); Jedis jedis = pool.getResource()) {
            return "PONG".equalsIgnoreCase(jedis.ping());
        } catch (Exception ignored) {
            return false;
        }
    }

    private SendMessageRequest newSendMessageRequest(String messageId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setRole("user");

        Part part = new Part();
        part.setPartId("p-" + messageId);
        part.setType("text");
        part.setText("hello " + messageId);
        message.setParts(List.of(part));

        SendMessageRequest request = new SendMessageRequest();
        request.setMessage(message);
        request.setIdempotencyKey("idem-" + messageId);
        return request;
    }
}
