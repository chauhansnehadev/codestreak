package com.codestreak.model;

import java.time.OffsetDateTime;

public class Submission {
    private int id;
    private int userId;
    private int problemId;
    private String problemTitle;
    private String status; // ACCEPTED, WRONG_ANSWER, etc.
    private String language;
    private Integer runtimeMs;
    private Integer memoryKb;
    private String notes;
    private OffsetDateTime submittedAt;

    public Submission() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(Integer runtimeMs) { this.runtimeMs = runtimeMs; }

    public Integer getMemoryKb() { return memoryKb; }
    public void setMemoryKb(Integer memoryKb) { this.memoryKb = memoryKb; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
}
