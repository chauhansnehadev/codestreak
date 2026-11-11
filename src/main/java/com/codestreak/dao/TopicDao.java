package com.codestreak.dao;

import com.codestreak.model.Topic;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TopicDao {

    public List<Topic> getAllTopics() throws SQLException {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT id, name, slug, description, icon, display_order, created_at FROM topics ORDER BY display_order ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                topics.add(mapTopic(rs));
            }
        }
        return topics;
    }

    public Optional<Topic> findById(int id) throws SQLException {
        String sql = "SELECT id, name, slug, description, icon, display_order, created_at FROM topics WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTopic(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Topic> getTopicsForProblem(int problemId) throws SQLException {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT t.id, t.name, t.slug, t.description, t.icon, t.display_order, t.created_at " +
                     "FROM topics t " +
                     "JOIN problem_topics pt ON t.id = pt.topic_id " +
                     "WHERE pt.problem_id = ? ORDER BY t.display_order ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, problemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(mapTopic(rs));
                }
            }
        }
        return topics;
    }

    private Topic mapTopic(ResultSet rs) throws SQLException {
        Topic t = new Topic();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setSlug(rs.getString("slug"));
        t.setDescription(rs.getString("description"));
        t.setIcon(rs.getString("icon"));
        t.setDisplayOrder(rs.getInt("display_order"));
        t.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return t;
    }
}
