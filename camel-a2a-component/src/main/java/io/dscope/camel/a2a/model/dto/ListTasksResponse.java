package io.dscope.camel.a2a.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import io.dscope.camel.a2a.model.Task;

/**
 * Result payload for ListTasks.
 */
public class ListTasksResponse {

    private List<Task> tasks;
    private String nextCursor;
    private int pageSize;
    private int totalSize;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @JsonProperty("nextPageToken")
    public String getNextCursor() {
        return nextCursor;
    }

    @JsonProperty("nextPageToken")
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
