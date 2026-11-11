package com.codestreak.model;

import java.time.OffsetDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private int targetProblemsPerDay;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public User() {}

    public User(int id, String username, String email, String passwordHash, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.targetProblemsPerDay = 3;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getTargetProblemsPerDay() { return targetProblemsPerDay; }
    public void setTargetProblemsPerDay(int targetProblemsPerDay) { this.targetProblemsPerDay = targetProblemsPerDay; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
