package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for GetTask.
 */
public class GetTaskResponse {

    private Task task;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
