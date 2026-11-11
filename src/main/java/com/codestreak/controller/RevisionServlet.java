package com.codestreak.controller;

import com.codestreak.model.Problem;
import com.codestreak.model.Revision;
import com.codestreak.model.User;
import com.codestreak.service.RevisionService;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/revisions/*")
public class RevisionServlet extends HttpServlet {

    private final RevisionService revisionService = new RevisionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String filter = req.getParameter("filter"); // PENDING, COMPLETED, OVERDUE, UPCOMING, ALL
                List<Revision> list = revisionService.getRevisions(user.getId(), filter);

                // Quick stats
                int pending = 0;
                int overdue = 0;
                int completed = 0;
                LocalDate today = LocalDate.now();

                List<Revision> all = revisionService.getRevisions(user.getId(), "ALL");
                for (Revision r : all) {
                    if ("COMPLETED".equalsIgnoreCase(r.getStatus())) {
                        completed++;
                    } else if ("PENDING".equalsIgnoreCase(r.getStatus())) {
                        pending++;
                        if (r.getScheduledFor().isBefore(today)) {
                            overdue++;
                        }
                    }
                }

                Map<String, Object> data = new HashMap<>();
                data.put("revisions", list);
                data.put("pendingCount", pending);
                data.put("overdueCount", overdue);
                data.put("completedCount", completed);

                JsonUtil.sendSuccess(resp, "Revisions list", data);
            } else if ("/suggestions".equalsIgnoreCase(pathInfo)) {
                List<Problem> suggestions = revisionService.getRevisionSuggestions(user.getId(), 5);
                JsonUtil.sendSuccess(resp, "Revision suggestions", suggestions);
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
            if (pathInfo == null || pathInfo.equals("/")) {
                // Schedule revision
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
                if (!body.has("problemId")) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "problemId is required");
                    return;
                }

                int problemId = body.get("problemId").asInt();
                LocalDate scheduledFor = body.has("scheduledFor") && !body.get("scheduledFor").asText().isEmpty() ?
                    LocalDate.parse(body.get("scheduledFor").asText()) : LocalDate.now().plusDays(3);
                int priority = body.has("priority") ? body.get("priority").asInt() : 1;
                String notes = body.has("notes") ? body.get("notes").asText() : "";

                Revision r = revisionService.scheduleRevision(user.getId(), problemId, scheduledFor, priority, notes);
                JsonUtil.sendCreated(resp, "Revision scheduled", r);
            } else if (pathInfo.matches("/\\d+/status")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
                String status = body.has("status") ? body.get("status").asText() : "COMPLETED";

                revisionService.updateStatus(id, user.getId(), status);
                JsonUtil.sendSuccess(resp, "Revision status updated", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
                revisionService.deleteRevision(id, user.getId());
                JsonUtil.sendSuccess(resp, "Revision deleted", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid revision ID");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
