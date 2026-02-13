package com.example.minitfg.network.models;

public class AuthResponse {
    private String message;
    private String token;
    private User user;
    private String error; // For error handling

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public String getError() { return error; }
}
