package com.codestreak.model;

import java.time.OffsetDateTime;

public class UserPlatform {
    private int id;
    private int userId;
    private int platformId;
    private String platformName;
    private String platformCode;
    private String platformUsername;
    private String profileUrl;
    private int problemsSolved;
    private int rating;
    private String globalRank;
    private OffsetDateTime lastSynced;
    private OffsetDateTime createdAt;

    public UserPlatform() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPlatformId() { return platformId; }
    public void setPlatformId(int platformId) { this.platformId = platformId; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }

    public String getPlatformUsername() { return platformUsername; }
    public void setPlatformUsername(String platformUsername) { this.platformUsername = platformUsername; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public int getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(int problemsSolved) { this.problemsSolved = problemsSolved; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getGlobalRank() { return globalRank; }
    public void setGlobalRank(String globalRank) { this.globalRank = globalRank; }

    public OffsetDateTime getLastSynced() { return lastSynced; }
    public void setLastSynced(OffsetDateTime lastSynced) { this.lastSynced = lastSynced; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
