package com.codestreak.controller;

import com.codestreak.dao.RoadmapDao;
import com.codestreak.dao.TopicDao;
import com.codestreak.model.RoadmapTopic;
import com.codestreak.model.Topic;
import com.codestreak.model.User;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/roadmap/*")
public class RoadmapServlet extends HttpServlet {

    private final RoadmapDao roadmapDao = new RoadmapDao();
    private final TopicDao topicDao = new TopicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Return default roadmap (id = 1) with user's progress
                List<RoadmapTopic> topics = roadmapDao.getRoadmapTopicsForUser(1, user.getId());

                // Calculate overall stats
                int totalRoadmapTopics = topics.size();
                int completedTopics = 0;
                int inProgressTopics = 0;
                for (RoadmapTopic rt : topics) {
                    if ("COMPLETED".equalsIgnoreCase(rt.getUserStatus())) {
                        completedTopics++;
                    } else if ("IN_PROGRESS".equalsIgnoreCase(rt.getUserStatus())) {
                        inProgressTopics++;
                    }
                }

                Map<String, Object> data = new HashMap<>();
                data.put("roadmapId", 1);
                data.put("title", "Standard DSA Mastery Roadmap");
                data.put("topics", topics);
                data.put("totalTopics", totalRoadmapTopics);
                data.put("completedTopics", completedTopics);
                data.put("inProgressTopics", inProgressTopics);
                data.put("progressPercent", totalRoadmapTopics > 0 ? Math.round(((double) completedTopics / totalRoadmapTopics) * 100) : 0);

                JsonUtil.sendSuccess(resp, "Roadmap loaded", data);
            } else if ("/topics".equalsIgnoreCase(pathInfo)) {
                // Return master topic list for dropdowns
                List<Topic> topics = topicDao.getAllTopics();
                JsonUtil.sendSuccess(resp, "All topics", topics);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+/status")) {
                int roadmapTopicId = Integer.parseInt(pathInfo.split("/")[1]);
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
                String status = body.has("status") ? body.get("status").asText() : "NOT_STARTED";

                roadmapDao.updateUserRoadmapTopicStatus(user.getId(), roadmapTopicId, status);
                JsonUtil.sendSuccess(resp, "Topic progress updated", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
