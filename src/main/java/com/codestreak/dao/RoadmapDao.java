package com.codestreak.dao;

import com.codestreak.model.RoadmapTopic;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoadmapDao {

    public List<RoadmapTopic> getRoadmapTopicsForUser(int roadmapId, int userId) throws SQLException {
        String sql = "SELECT rt.id as roadmap_topic_id, rt.roadmap_id, rt.topic_id, t.name as topic_name, " +
                     "t.slug as topic_slug, t.description as topic_description, t.icon as topic_icon, " +
                     "rt.sequence_order, rt.target_problems, " +
                     "COALESCE(urt.status, 'NOT_STARTED') as user_status, " +
                     "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' THEN up.problem_id END) as solved_count, " +
                     "COUNT(DISTINCT pt.problem_id) as total_problems_count " +
                     "FROM roadmap_topics rt " +
                     "JOIN topics t ON rt.topic_id = t.id " +
                     "LEFT JOIN user_roadmap_topics urt ON rt.id = urt.roadmap_topic_id AND urt.user_id = ? " +
                     "LEFT JOIN problem_topics pt ON pt.topic_id = t.id " +
                     "LEFT JOIN user_problems up ON up.problem_id = pt.problem_id AND up.user_id = ? " +
                     "WHERE rt.roadmap_id = ? " +
                     "GROUP BY rt.id, rt.roadmap_id, rt.topic_id, t.name, t.slug, t.description, t.icon, rt.sequence_order, rt.target_problems, urt.status " +
                     "ORDER BY rt.sequence_order ASC";

        List<RoadmapTopic> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, roadmapId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoadmapTopic rt = new RoadmapTopic();
                    rt.setId(rs.getInt("roadmap_topic_id"));
                    rt.setRoadmapId(rs.getInt("roadmap_id"));
                    rt.setTopicId(rs.getInt("topic_id"));
                    rt.setTopicName(rs.getString("topic_name"));
                    rt.setTopicSlug(rs.getString("topic_slug"));
                    rt.setTopicDescription(rs.getString("topic_description"));
                    rt.setTopicIcon(rs.getString("topic_icon"));
                    rt.setSequenceOrder(rs.getInt("sequence_order"));
                    rt.setTargetProblems(rs.getInt("target_problems"));
                    rt.setUserStatus(rs.getString("user_status"));
                    rt.setSolvedProblemsCount(rs.getInt("solved_count"));
                    rt.setTotalProblemsCount(rs.getInt("total_problems_count"));
                    list.add(rt);
                }
            }
        }
        return list;
    }

    public void updateUserRoadmapTopicStatus(int userId, int roadmapTopicId, String status) throws SQLException {
        String sql = "INSERT INTO user_roadmap_topics (user_id, roadmap_topic_id, status, updated_at) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (user_id, roadmap_topic_id) DO UPDATE SET " +
                     "status = EXCLUDED.status, updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, roadmapTopicId);
            ps.setString(3, status);
            ps.executeUpdate();
        }
    }
}
