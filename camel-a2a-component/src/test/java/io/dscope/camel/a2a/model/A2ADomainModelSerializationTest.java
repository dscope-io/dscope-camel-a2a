package io.dscope.camel.a2a.model;

import java.util.List;
import java.util.Map;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.CancelTaskResponse;
import io.dscope.camel.a2a.model.dto.GetTaskRequest;
import io.dscope.camel.a2a.model.dto.GetTaskResponse;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.ListTasksResponse;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;
import io.dscope.camel.a2a.model.dto.SendMessageResponse;
import io.dscope.camel.a2a.service.A2AJsonCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2ADomainModelSerializationTest {

    private final A2AJsonCodec codec = new A2AJsonCodec();

    @Test
    void roundTripTaskAndNestedModels() throws Exception {
        Part part = samplePart();
        Message message = sampleMessage(part);
        Artifact artifact = sampleArtifact(part);
        TaskStatus status = sampleStatus();

        Task task = new Task();
        task.setTaskId("task-1");
        task.setStatus(status);
        task.setLatestMessage(message);
        task.setMessages(List.of(message));
        task.setArtifacts(List.of(artifact));
        task.setMetadata(Map.of("priority", "high"));
        task.setCreatedAt("2026-02-08T00:00:00Z");
        task.setUpdatedAt("2026-02-08T00:10:00Z");

        String json = codec.serialize(task);
        Task restored = codec.deserialize(json, Task.class);

        assertEquals("task-1", restored.getTaskId());
        assertEquals(TaskState.RUNNING, restored.getStatus().getState());
        assertEquals("assistant", restored.getLatestMessage().getRole());
        assertEquals("text", restored.getLatestMessage().getParts().get(0).getType());
        assertEquals("artifact-1", restored.getArtifacts().get(0).getArtifactId());
        assertEquals("high", restored.getMetadata().get("priority"));
    }

    @Test
    void roundTripAgentCardWithCapabilitiesAndSecurity() throws Exception {
        AgentCapabilities capabilities = new AgentCapabilities();
        capabilities.setStreaming(true);
        capabilities.setPushNotifications(true);
        capabilities.setStatefulTasks(true);
        capabilities.setSupportedMethods(List.of("SendMessage", "GetTask", "ListTasks", "CancelTask"));

        AgentSecurityScheme bearer = new AgentSecurityScheme();
        bearer.setType("http");
        bearer.setScheme("bearer");
        bearer.setDescription("JWT bearer token");
        bearer.setScopes(List.of("tasks.read", "tasks.write"));

        AgentCard card = new AgentCard();
        card.setAgentId("assistant-agent");
        card.setName("Assistant Agent");
        card.setDescription("Handles task-oriented requests");
        card.setEndpointUrl("https://example.org/a2a");
        card.setVersion("0.5.0");
        card.setCapabilities(capabilities);
        card.setSecuritySchemes(Map.of("bearerAuth", bearer));
        card.setDefaultInputModes(List.of("text/plain", "application/json"));
        card.setDefaultOutputModes(List.of("text/plain", "application/json"));
        card.setMetadata(Map.of("team", "dscope"));

        String json = codec.serialize(card);
        AgentCard restored = codec.deserialize(json, AgentCard.class);

        assertEquals("assistant-agent", restored.getAgentId());
        assertTrue(restored.getCapabilities().isStreaming());
        assertTrue(restored.getCapabilities().isPushNotifications());
        assertEquals("bearer", restored.getSecuritySchemes().get("bearerAuth").getScheme());
        assertEquals("dscope", restored.getMetadata().get("team"));
    }

    @Test
    void roundTripCoreMethodDtos() throws Exception {
        Task task = new Task();
        task.setTaskId("task-22");
        task.setStatus(sampleStatus());

        SendMessageRequest sendRequest = new SendMessageRequest();
        sendRequest.setMessage(sampleMessage(samplePart()));
        sendRequest.setConversationId("conv-1");
        sendRequest.setIdempotencyKey("idem-1");
        sendRequest.setMetadata(Map.of("source", "api"));

        SendMessageResponse sendResponse = new SendMessageResponse();
        sendResponse.setTask(task);

        GetTaskRequest getTaskRequest = new GetTaskRequest();
        getTaskRequest.setTaskId("task-22");
        GetTaskResponse getTaskResponse = new GetTaskResponse();
        getTaskResponse.setTask(task);

        ListTasksRequest listTasksRequest = new ListTasksRequest();
        listTasksRequest.setLimit(50);
        listTasksRequest.setCursor("cursor-1");
        listTasksRequest.setState("RUNNING");
        ListTasksResponse listTasksResponse = new ListTasksResponse();
        listTasksResponse.setTasks(List.of(task));
        listTasksResponse.setNextCursor("cursor-2");

        CancelTaskRequest cancelTaskRequest = new CancelTaskRequest();
        cancelTaskRequest.setTaskId("task-22");
        cancelTaskRequest.setReason("user-request");
        CancelTaskResponse cancelTaskResponse = new CancelTaskResponse();
        cancelTaskResponse.setTask(task);
        cancelTaskResponse.setCanceled(true);

        SendMessageRequest restoredSendRequest = codec.deserialize(codec.serialize(sendRequest), SendMessageRequest.class);
        SendMessageResponse restoredSendResponse = codec.deserialize(codec.serialize(sendResponse), SendMessageResponse.class);
        GetTaskRequest restoredGetTaskRequest = codec.deserialize(codec.serialize(getTaskRequest), GetTaskRequest.class);
        GetTaskResponse restoredGetTaskResponse = codec.deserialize(codec.serialize(getTaskResponse), GetTaskResponse.class);
        ListTasksRequest restoredListTasksRequest = codec.deserialize(codec.serialize(listTasksRequest), ListTasksRequest.class);
        ListTasksResponse restoredListTasksResponse = codec.deserialize(codec.serialize(listTasksResponse), ListTasksResponse.class);
        CancelTaskRequest restoredCancelTaskRequest = codec.deserialize(codec.serialize(cancelTaskRequest), CancelTaskRequest.class);
        CancelTaskResponse restoredCancelTaskResponse = codec.deserialize(codec.serialize(cancelTaskResponse), CancelTaskResponse.class);

        assertEquals("conv-1", restoredSendRequest.getConversationId());
        assertEquals("task-22", restoredSendResponse.getTask().getTaskId());
        assertEquals("task-22", restoredGetTaskRequest.getTaskId());
        assertEquals(TaskState.RUNNING, restoredGetTaskResponse.getTask().getStatus().getState());
        assertEquals(50, restoredListTasksRequest.getLimit());
        assertEquals("cursor-2", restoredListTasksResponse.getNextCursor());
        assertEquals("user-request", restoredCancelTaskRequest.getReason());
        assertTrue(restoredCancelTaskResponse.isCanceled());
    }

    private Part samplePart() {
        Part part = new Part();
        part.setPartId("part-1");
        part.setType("text");
        part.setMimeType("text/plain");
        part.setText("hello");
        part.setData(Map.of("length", 5));
        part.setMetadata(Map.of("lang", "en"));
        return part;
    }

    private Message sampleMessage(Part part) {
        Message message = new Message();
        message.setMessageId("msg-1");
        message.setRole("assistant");
        message.setInReplyTo("msg-0");
        message.setParts(List.of(part));
        message.setMetadata(Map.of("channel", "chat"));
        message.setCreatedAt("2026-02-08T00:05:00Z");
        return message;
    }

    private Artifact sampleArtifact(Part part) {
        Artifact artifact = new Artifact();
        artifact.setArtifactId("artifact-1");
        artifact.setName("summary");
        artifact.setDescription("Task summary");
        artifact.setParts(List.of(part));
        artifact.setMetadata(Map.of("format", "markdown"));
        artifact.setCreatedAt("2026-02-08T00:08:00Z");
        return artifact;
    }

    private TaskStatus sampleStatus() {
        TaskStatus status = new TaskStatus();
        status.setState(TaskState.RUNNING);
        status.setMessage("processing");
        status.setUpdatedAt("2026-02-08T00:07:00Z");
        status.setDetails(Map.of("progress", 42));
        return status;
    }
}
