package com.example.minitfg.network.models;

import com.google.gson.annotations.SerializedName;

public class Score {
    private int id;
    @SerializedName("user_id")
    private int userId;
    private String email;
    private String username;
    private String subject;
    private int score;
    private long timestamp;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getSubject() {
        return subject;
    }

    public int getScore() {
        return score;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
