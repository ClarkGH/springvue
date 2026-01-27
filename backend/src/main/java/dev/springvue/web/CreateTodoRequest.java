package dev.springvue.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTodoRequest {

    private final String title;

    @JsonCreator
    public CreateTodoRequest(@JsonProperty("title") String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
