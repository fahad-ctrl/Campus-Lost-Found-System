package com.campus.lostfound.model;

import java.time.LocalDateTime;

public class Notification {

    private int id;
    private int userId;
    private Integer matchId;   // nullable
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(int userId, Integer matchId, String message) {
        this.userId = userId;
        this.matchId = matchId;
        this.message = message;
        this.read = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
