package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for ListTasks.
 */
public class ListTasksRequest {

    private Integer limit;
    private String cursor;
    private String state;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
