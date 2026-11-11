package com.codestreak.controller;

import com.codestreak.dao.AnalyticsDao;
import com.codestreak.model.DashboardSummary;
import com.codestreak.model.Submission;
import com.codestreak.model.User;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/analytics/*")
public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsDao analyticsDao = new AnalyticsDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/overview".equalsIgnoreCase(pathInfo)) {
                DashboardSummary summary = analyticsDao.getDashboardSummary(user.getId());
                List<Submission> recent = analyticsDao.getRecentSubmissions(user.getId(), 8);

                Map<String, Object> data = new HashMap<>();
                data.put("summary", summary);
                data.put("recentSubmissions", recent);

                JsonUtil.sendSuccess(resp, "Analytics overview", data);
            } else if ("/topics".equalsIgnoreCase(pathInfo)) {
                List<Map<String, Object>> topicPerformance = analyticsDao.getTopicPerformance(user.getId());
                JsonUtil.sendSuccess(resp, "Topic performance", topicPerformance);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
