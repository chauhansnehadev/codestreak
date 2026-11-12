package com.codestreak.model;

import java.util.HashMap;
import java.util.Map;

public class DashboardSummary {
    private int totalSolved;
    private int totalAttempted;
    private int totalProblems;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int currentStreak;
    private int longestStreak;
    private int pendingRevisions;
    private Map<String, Integer> platformSolved = new HashMap<>();
    private java.util.List<UserPlatform> connectedPlatforms = new java.util.ArrayList<>();
    private int externalTotalSolved;
    private Map<String, Integer> topicSolved = new HashMap<>();
    private Map<String, Integer> weeklyActivity = new HashMap<>(); // "YYYY-MM-DD" -> count

    public DashboardSummary() {}

    public int getTotalSolved() { return totalSolved; }
    public void setTotalSolved(int totalSolved) { this.totalSolved = totalSolved; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getTotalProblems() { return totalProblems; }
    public void setTotalProblems(int totalProblems) { this.totalProblems = totalProblems; }

    public int getEasySolved() { return easySolved; }
    public void setEasySolved(int easySolved) { this.easySolved = easySolved; }

    public int getMediumSolved() { return mediumSolved; }
    public void setMediumSolved(int mediumSolved) { this.mediumSolved = mediumSolved; }

    public int getHardSolved() { return hardSolved; }
    public void setHardSolved(int hardSolved) { this.hardSolved = hardSolved; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public int getPendingRevisions() { return pendingRevisions; }
    public void setPendingRevisions(int pendingRevisions) { this.pendingRevisions = pendingRevisions; }

    public Map<String, Integer> getPlatformSolved() { return platformSolved; }
    public void setPlatformSolved(Map<String, Integer> platformSolved) { this.platformSolved = platformSolved; }

    public java.util.List<UserPlatform> getConnectedPlatforms() { return connectedPlatforms; }
    public void setConnectedPlatforms(java.util.List<UserPlatform> connectedPlatforms) { this.connectedPlatforms = connectedPlatforms; }

    public int getExternalTotalSolved() { return externalTotalSolved; }
    public void setExternalTotalSolved(int externalTotalSolved) { this.externalTotalSolved = externalTotalSolved; }

    public Map<String, Integer> getTopicSolved() { return topicSolved; }
    public void setTopicSolved(Map<String, Integer> topicSolved) { this.topicSolved = topicSolved; }

    public Map<String, Integer> getWeeklyActivity() { return weeklyActivity; }
    public void setWeeklyActivity(Map<String, Integer> weeklyActivity) { this.weeklyActivity = weeklyActivity; }
}
