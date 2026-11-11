package com.codestreak.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class Revision {
    private int id;
    private int userId;
    private int problemId;
    private String problemTitle;
    private String problemUrl;
    private String problemDifficulty;
    private LocalDate scheduledFor;
    private String status; // PENDING, COMPLETED, SKIPPED
    private int priority; // 1: Normal, 2: High, 3: Urgent
    private String notes;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;

    public Revision() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public String getProblemUrl() { return problemUrl; }
    public void setProblemUrl(String problemUrl) { this.problemUrl = problemUrl; }

    public String getProblemDifficulty() { return problemDifficulty; }
    public void setProblemDifficulty(String problemDifficulty) { this.problemDifficulty = problemDifficulty; }

    public LocalDate getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDate scheduledFor) { this.scheduledFor = scheduledFor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
