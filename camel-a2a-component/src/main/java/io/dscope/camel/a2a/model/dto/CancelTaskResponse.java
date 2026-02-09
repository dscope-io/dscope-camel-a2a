package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for CancelTask.
 */
public class CancelTaskResponse {

    private Task task;
    private boolean canceled;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
