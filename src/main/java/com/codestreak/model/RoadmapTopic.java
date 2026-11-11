package com.codestreak.model;

import java.util.ArrayList;
import java.util.List;

public class RoadmapTopic {
    private int id;
    private int roadmapId;
    private int topicId;
    private String topicName;
    private String topicSlug;
    private String topicDescription;
    private String topicIcon;
    private int sequenceOrder;
    private int targetProblems;
    private String userStatus; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private int solvedProblemsCount;
    private int totalProblemsCount;

    public RoadmapTopic() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoadmapId() { return roadmapId; }
    public void setRoadmapId(int roadmapId) { this.roadmapId = roadmapId; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getTopicSlug() { return topicSlug; }
    public void setTopicSlug(String topicSlug) { this.topicSlug = topicSlug; }

    public String getTopicDescription() { return topicDescription; }
    public void setTopicDescription(String topicDescription) { this.topicDescription = topicDescription; }

    public String getTopicIcon() { return topicIcon; }
    public void setTopicIcon(String topicIcon) { this.topicIcon = topicIcon; }

    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public int getTargetProblems() { return targetProblems; }
    public void setTargetProblems(int targetProblems) { this.targetProblems = targetProblems; }

    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String userStatus) { this.userStatus = userStatus; }

    public int getSolvedProblemsCount() { return solvedProblemsCount; }
    public void setSolvedProblemsCount(int solvedProblemsCount) { this.solvedProblemsCount = solvedProblemsCount; }

    public int getTotalProblemsCount() { return totalProblemsCount; }
    public void setTotalProblemsCount(int totalProblemsCount) { this.totalProblemsCount = totalProblemsCount; }
}
