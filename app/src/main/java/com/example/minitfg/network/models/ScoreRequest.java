package com.example.minitfg.network.models;

public class ScoreRequest {
    private String subject;
    private int score;

    public ScoreRequest(String subject, int score) {
        this.subject = subject;
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public int getScore() {
        return score;
    }
}
