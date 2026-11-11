package com.codestreak.controller;

import com.codestreak.model.Recommendation;
import com.codestreak.model.User;
import com.codestreak.service.RecommendationService;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/recommendations/*")
public class RecommendationServlet extends HttpServlet {

    private final RecommendationService recommendationService = new RecommendationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        try {
            int limit = 6;
            if (req.getParameter("limit") != null) {
                limit = Integer.parseInt(req.getParameter("limit"));
            }

            List<Recommendation> recommendations = recommendationService.getRecommendationsForUser(user.getId(), limit);
            JsonUtil.sendSuccess(resp, "Recommendations retrieved", recommendations);
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
