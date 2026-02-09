package io.dscope.camel.a2a.service;

import io.dscope.camel.a2a.model.Task;
import io.dscope.camel.a2a.model.TaskStatus;
import io.dscope.camel.a2a.model.TaskState;
import io.dscope.camel.a2a.model.dto.CancelTaskRequest;
import io.dscope.camel.a2a.model.dto.ListTasksRequest;
import io.dscope.camel.a2a.model.dto.SendMessageRequest;

import java.util.List;

/**
 * Core task operations used by A2A method processors.
 */
public interface A2ATaskService {

    Task sendMessage(SendMessageRequest request);

    Task getTask(String taskId);

    List<Task> listTasks(ListTasksRequest request);

    Task cancelTask(CancelTaskRequest request);

    Task transitionTask(String taskId, TaskState targetState, String reason);

    List<TaskStatus> getTaskHistory(String taskId);
}
