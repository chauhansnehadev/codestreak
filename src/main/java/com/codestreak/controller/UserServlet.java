package com.codestreak.controller;

import com.codestreak.model.User;
import com.codestreak.service.UserService;
import com.codestreak.util.AuthHelper;
import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/user/*")
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        try {
            User current = userService.getUserById(user.getId()).orElse(user);
            current.setPasswordHash(null);
            JsonUtil.sendSuccess(resp, "User profile", current);
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        String pathInfo = req.getPathInfo();

        try {
            JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
            if ("/password".equalsIgnoreCase(pathInfo)) {
                String oldPass = body.has("oldPassword") ? body.get("oldPassword").asText() : "";
                String newPass = body.has("newPassword") ? body.get("newPassword").asText() : "";
                boolean success = userService.changePassword(user.getId(), oldPass, newPass);
                if (success) {
                    JsonUtil.sendSuccess(resp, "Password updated successfully", null);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to update password");
                }
            } else {
                // Update profile details
                if (body.has("fullName")) {
                    user.setFullName(body.get("fullName").asText().trim());
                }
                if (body.has("targetProblemsPerDay")) {
                    user.setTargetProblemsPerDay(body.get("targetProblemsPerDay").asInt());
                }
                if (body.has("avatarUrl")) {
                    user.setAvatarUrl(body.get("avatarUrl").asText().trim());
                }

                boolean updated = userService.updateProfile(user);
                if (updated) {
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        session.setAttribute("currentUser", user);
                    }
                    user.setPasswordHash(null);
                    JsonUtil.sendSuccess(resp, "Profile updated successfully", user);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not update profile");
                }
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
