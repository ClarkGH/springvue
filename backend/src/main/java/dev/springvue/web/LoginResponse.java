package dev.springvue.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

    private final String token;
    private final String username;

    @JsonCreator
    public LoginResponse(@JsonProperty("token") String token,
                         @JsonProperty("username") String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}
