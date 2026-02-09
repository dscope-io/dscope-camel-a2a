package io.dscope.camel.a2a.model.dto;

import java.util.List;
import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for ListTasks.
 */
public class ListTasksResponse {

    private List<Task> tasks;
    private String nextCursor;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}
