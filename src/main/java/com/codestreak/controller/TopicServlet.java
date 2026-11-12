package com.codestreak.controller;

import com.codestreak.dao.TopicDao;
import com.codestreak.model.Topic;
import com.codestreak.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/topics/*")
public class TopicServlet extends HttpServlet {

    private final TopicDao topicDao = new TopicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Topic> topics = topicDao.getAllTopics();
            JsonUtil.sendSuccess(resp, "All topics", topics);
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
