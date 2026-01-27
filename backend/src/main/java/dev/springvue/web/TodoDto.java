package dev.springvue.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.springvue.entity.Todo;

import java.time.Instant;

public class TodoDto {

    private final Long id;
    private final String title;
    private final boolean completed;
    private final Instant createdAt;

    @JsonCreator
    public TodoDto(@JsonProperty("id") Long id,
                  @JsonProperty("title") String title,
                  @JsonProperty("completed") boolean completed,
                  @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public static TodoDto from(Todo todo) {
        return new TodoDto(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
