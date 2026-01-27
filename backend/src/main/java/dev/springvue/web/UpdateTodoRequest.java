package dev.springvue.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTodoRequest {

    private final String title;
    private final Boolean completed;

    @JsonCreator
    public UpdateTodoRequest(@JsonProperty("title") String title,
                            @JsonProperty("completed") Boolean completed) {
        this.title = title;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getCompleted() {
        return completed;
    }
}
