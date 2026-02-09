package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for SendMessage.
 */
public class SendMessageResponse {

    private Task task;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
