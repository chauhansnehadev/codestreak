package com.codestreak.controller;

import com.codestreak.dao.PlatformDao;
import com.codestreak.model.Platform;
import com.codestreak.model.User;
import com.codestreak.model.UserPlatform;
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

@WebServlet("/api/platforms/*")
public class PlatformServlet extends HttpServlet {

    private final PlatformDao platformDao = new PlatformDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        try {
            List<Platform> masterList = platformDao.getAllPlatforms();
            List<UserPlatform> userProfiles = platformDao.getUserPlatforms(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("platforms", masterList);
            data.put("userPlatforms", userProfiles);

            JsonUtil.sendSuccess(resp, "Platforms retrieved", data);
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthHelper.getEffectiveUser(req);
        try {
            JsonNode body = JsonUtil.getMapper().readTree(req.getInputStream());
            if (!body.has("platformId") || !body.has("platformUsername")) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "platformId and platformUsername are required.");
                return;
            }

            UserPlatform up = new UserPlatform();
            up.setUserId(user.getId());
            up.setPlatformId(body.get("platformId").asInt());
            up.setPlatformUsername(body.get("platformUsername").asText().trim());
            up.setProfileUrl(body.has("profileUrl") ? body.get("profileUrl").asText().trim() : null);
            up.setProblemsSolved(body.has("problemsSolved") ? body.get("problemsSolved").asInt() : 0);
            up.setRating(body.has("rating") ? body.get("rating").asInt() : 0);
            up.setGlobalRank(body.has("globalRank") ? body.get("globalRank").asText() : null);

            platformDao.upsertUserPlatform(up);
            JsonUtil.sendSuccess(resp, "Platform profile saved successfully", up);
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
                int platformId = Integer.parseInt(pathInfo.substring(1));
                platformDao.deleteUserPlatform(user.getId(), platformId);
                JsonUtil.sendSuccess(resp, "Platform profile disconnected", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid platform ID");
            }
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
