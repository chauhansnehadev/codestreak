package com.codestreak.controller;

import com.codestreak.model.Problem;
import com.codestreak.model.User;
import com.codestreak.service.ProblemService;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/problems/*")
public class ProblemServlet extends HttpServlet {

    private final ProblemService problemService = new ProblemService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List problems with optional filters
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                String difficulty = req.getParameter("difficulty");
                Integer topicId = req.getParameter("topicId") != null ? Integer.parseInt(req.getParameter("topicId")) : null;
                Integer platformId = req.getParameter("platformId") != null ? Integer.parseInt(req.getParameter("platformId")) : null;

                List<Problem> list = problemService.listUserProblems(user.getId(), search, status, difficulty, topicId, platformId);
                JsonUtil.sendSuccess(resp, "Problems retrieved", list);
            } else {
                // Single problem: /api/problems/{id}
                int id = Integer.parseInt(pathInfo.substring(1));
                Optional<Problem> problem = problemService.getProblem(id, user.getId());
                if (problem.isPresent()) {
                    JsonUtil.sendSuccess(resp, "Problem details", problem.get());
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Problem not found");
                }
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid problem ID format");
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Create problem
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
                Problem p = new Problem();
                p.setTitle(body.has("title") ? body.get("title").asText() : "");
                p.setUrl(body.has("url") ? body.get("url").asText() : null);
                p.setDifficulty(body.has("difficulty") ? body.get("difficulty").asText() : "MEDIUM");
                if (body.has("platformId") && !body.get("platformId").isNull()) {
                    p.setPlatformId(body.get("platformId").asInt());
                }
                if (body.has("status")) {
                    p.setUserStatus(body.get("status").asText());
                }
                if (body.has("notes")) {
                    p.setUserNotes(body.get("notes").asText());
                }

                List<Integer> topicIds = new ArrayList<>();
                if (body.has("topicIds") && body.get("topicIds").isArray()) {
                    for (JsonNode t : body.get("topicIds")) {
                        topicIds.add(t.asInt());
                    }
                }

                Problem created = problemService.createProblem(p, topicIds, user.getId());
                JsonUtil.sendCreated(resp, "Problem added successfully", created);
            } else if (pathInfo.matches("/\\d+/status")) {
                // Update problem status: /api/problems/{id}/status
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
                String status = body.has("status") ? body.get("status").asText() : "NOT_STARTED";
                String notes = body.has("notes") ? body.get("notes").asText() : null;

                problemService.updateStatus(user.getId(), id, status, notes);
                JsonUtil.sendSuccess(resp, "Problem status updated", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                int id = Integer.parseInt(pathInfo.substring(1));
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());

                Problem p = new Problem();
                p.setId(id);
                p.setTitle(body.has("title") ? body.get("title").asText() : "");
                p.setUrl(body.has("url") ? body.get("url").asText() : null);
                p.setDifficulty(body.has("difficulty") ? body.get("difficulty").asText() : "MEDIUM");
                if (body.has("platformId") && !body.get("platformId").isNull()) {
                    p.setPlatformId(body.get("platformId").asInt());
                }

                List<Integer> topicIds = new ArrayList<>();
                if (body.has("topicIds") && body.get("topicIds").isArray()) {
                    for (JsonNode t : body.get("topicIds")) {
                        topicIds.add(t.asInt());
                    }
                }

                boolean updated = problemService.updateProblem(p, topicIds, user.getId());
                if (updated) {
                    if (body.has("status") || body.has("notes")) {
                        String status = body.has("status") ? body.get("status").asText() : "NOT_STARTED";
                        String notes = body.has("notes") ? body.get("notes").asText() : null;
                        problemService.updateStatus(user.getId(), id, status, notes);
                    }
                    JsonUtil.sendSuccess(resp, "Problem updated successfully", null);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Not authorized to edit this problem");
                }
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing problem ID");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                int id = Integer.parseInt(pathInfo.substring(1));
                boolean deleted = problemService.deleteProblem(id, user.getId());
                if (deleted) {
                    JsonUtil.sendSuccess(resp, "Problem removed successfully", null);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Problem not found");
                }
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid problem ID");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
