package com.codestreak.model;

import java.util.ArrayList;
import java.util.List;

public class Recommendation {
    private Problem problem;
    private Topic topic;
    private double weaknessScore;
    private String rationale;
    private String priorityLabel; // HIGH, MEDIUM, LOW

    public Recommendation() {}

    public Recommendation(Problem problem, Topic topic, double weaknessScore, String rationale, String priorityLabel) {
        this.problem = problem;
        this.topic = topic;
        this.weaknessScore = weaknessScore;
        this.rationale = rationale;
        this.priorityLabel = priorityLabel;
    }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public double getWeaknessScore() { return weaknessScore; }
    public void setWeaknessScore(double weaknessScore) { this.weaknessScore = weaknessScore; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public String getPriorityLabel() { return priorityLabel; }
    public void setPriorityLabel(String priorityLabel) { this.priorityLabel = priorityLabel; }
}
