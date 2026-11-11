package com.codestreak.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Problem {
    private int id;
    private String title;
    private String url;
    private Integer platformId;
    private String platformName;
    private String difficulty; // EASY, MEDIUM, HARD
    private boolean custom;
    private Integer createdByUserId;
    private OffsetDateTime createdAt;

    // Associated topics
    private List<Topic> topics = new ArrayList<>();

    // User-specific status when queried in context of a user
    private String userStatus; // NOT_STARTED, ATTEMPTED, SOLVED, REVISION_NEEDED
    private String userNotes;
    private OffsetDateTime firstSolvedAt;
    private OffsetDateTime lastAttemptedAt;

    public Problem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getPlatformId() { return platformId; }
    public void setPlatformId(Integer platformId) { this.platformId = platformId; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public boolean isCustom() { return custom; }
    public void setCustom(boolean custom) { this.custom = custom; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String userStatus) { this.userStatus = userStatus; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public OffsetDateTime getFirstSolvedAt() { return firstSolvedAt; }
    public void setFirstSolvedAt(OffsetDateTime firstSolvedAt) { this.firstSolvedAt = firstSolvedAt; }

    public OffsetDateTime getLastAttemptedAt() { return lastAttemptedAt; }
    public void setLastAttemptedAt(OffsetDateTime lastAttemptedAt) { this.lastAttemptedAt = lastAttemptedAt; }
}
