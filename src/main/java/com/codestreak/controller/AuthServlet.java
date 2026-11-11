package com.codestreak.controller;

import com.codestreak.model.User;
import com.codestreak.service.UserService;
import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/login".equalsIgnoreCase(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/register".equalsIgnoreCase(pathInfo)) {
            handleRegister(req, resp);
        } else if ("/logout".equalsIgnoreCase(pathInfo)) {
            handleLogout(req, resp);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/me".equalsIgnoreCase(pathInfo)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                User user = (User) session.getAttribute("currentUser");
                // Avoid returning password hash in response
                user.setPasswordHash(null);
                JsonUtil.sendSuccess(resp, "Authenticated user details", user);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            }
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
            if (body == null || !body.has("username") || !body.has("password")) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
                return;
            }

            String username = body.get("username").asText().trim();
            String password = body.get("password").asText();

            Optional<User> authUser = userService.authenticate(username, password);
            if (authUser.isPresent()) {
                User user = authUser.get();
                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", user);

                Map<String, Object> respData = new HashMap<>();
                respData.put("id", user.getId());
                respData.put("username", user.getUsername());
                respData.put("email", user.getEmail());
                respData.put("fullName", user.getFullName());
                respData.put("targetProblemsPerDay", user.getTargetProblemsPerDay());

                JsonUtil.sendSuccess(resp, "Login successful", respData);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
            if (body == null || !body.has("username") || !body.has("email") || !body.has("password")) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username, email, and password are required.");
                return;
            }

            String username = body.get("username").asText();
            String email = body.get("email").asText();
            String password = body.get("password").asText();
            String fullName = body.has("fullName") ? body.get("fullName").asText() : username;

            User created = userService.register(username, email, password, fullName);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", created);

            created.setPasswordHash(null);
            JsonUtil.sendCreated(resp, "Account created successfully", created);
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.sendSuccess(resp, "Logged out successfully", null);
    }
}
