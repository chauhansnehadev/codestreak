package com.codestreak.dao;

import com.codestreak.model.Problem;
import com.codestreak.model.Topic;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProblemDao {

    public List<Problem> findUserProblems(int userId, String search, String status, String difficulty, Integer topicId, Integer platformId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT p.id, p.title, p.url, p.platform_id, pl.name as platform_name, p.difficulty, " +
            "p.is_custom, p.created_by_user_id, p.created_at, " +
            "COALESCE(up.status, 'NOT_STARTED') as user_status, up.notes as user_notes, " +
            "up.first_solved_at, up.last_attempted_at " +
            "FROM problems p " +
            "LEFT JOIN platforms pl ON p.platform_id = pl.id " +
            "LEFT JOIN user_problems up ON p.id = up.problem_id AND up.user_id = ? " +
            "LEFT JOIN problem_topics pt ON p.id = pt.problem_id " +
            "WHERE (p.is_custom = FALSE OR p.created_by_user_id = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND LOWER(p.title) LIKE ? ");
            params.add("%" + search.trim().toLowerCase() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("NOT_STARTED".equalsIgnoreCase(status)) {
                sql.append("AND (up.status IS NULL OR up.status = 'NOT_STARTED') ");
            } else {
                sql.append("AND up.status = ? ");
                params.add(status.toUpperCase());
            }
        }
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            sql.append("AND p.difficulty = ? ");
            params.add(difficulty.toUpperCase());
        }
        if (topicId != null && topicId > 0) {
            sql.append("AND pt.topic_id = ? ");
            params.add(topicId);
        }
        if (platformId != null && platformId > 0) {
            sql.append("AND p.platform_id = ? ");
            params.add(platformId);
        }

        sql.append("ORDER BY p.id ASC");

        List<Problem> problems = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    problems.add(mapProblem(rs));
                }
            }
        }

        // Fetch topics for each problem
        TopicDao topicDao = new TopicDao();
        for (Problem prob : problems) {
            prob.setTopics(topicDao.getTopicsForProblem(prob.getId()));
        }

        return problems;
    }

    public Optional<Problem> findById(int id, int userId) throws SQLException {
        String sql = "SELECT p.id, p.title, p.url, p.platform_id, pl.name as platform_name, p.difficulty, " +
                     "p.is_custom, p.created_by_user_id, p.created_at, " +
                     "COALESCE(up.status, 'NOT_STARTED') as user_status, up.notes as user_notes, " +
                     "up.first_solved_at, up.last_attempted_at " +
                     "FROM problems p " +
                     "LEFT JOIN platforms pl ON p.platform_id = pl.id " +
                     "LEFT JOIN user_problems up ON p.id = up.problem_id AND up.user_id = ? " +
                     "WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Problem p = mapProblem(rs);
                    TopicDao topicDao = new TopicDao();
                    p.setTopics(topicDao.getTopicsForProblem(p.getId()));
                    return Optional.of(p);
                }
            }
        }
        return Optional.empty();
    }

    public Problem createProblem(Problem problem, List<Integer> topicIds) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO problems (title, url, platform_id, difficulty, is_custom, created_by_user_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, problem.getTitle());
                ps.setString(2, problem.getUrl());
                if (problem.getPlatformId() != null && problem.getPlatformId() > 0) {
                    ps.setInt(3, problem.getPlatformId());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, problem.getDifficulty());
                ps.setBoolean(5, problem.isCustom());
                if (problem.getCreatedByUserId() != null) {
                    ps.setInt(6, problem.getCreatedByUserId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        problem.setId(rs.getInt("id"));
                        problem.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    }
                }
            }

            // Link problem topics
            if (topicIds != null && !topicIds.isEmpty()) {
                String ptSql = "INSERT INTO problem_topics (problem_id, topic_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
                try (PreparedStatement ptPs = conn.prepareStatement(ptSql)) {
                    for (Integer tid : topicIds) {
                        ptPs.setInt(1, problem.getId());
                        ptPs.setInt(2, tid);
                        ptPs.addBatch();
                    }
                    ptPs.executeBatch();
                }
            }

            conn.commit();
            return problem;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public boolean updateProblem(Problem problem, List<Integer> topicIds, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Check if user is authorized to edit custom problem
            String checkSql = "SELECT created_by_user_id, is_custom FROM problems WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, problem.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        boolean isCustom = rs.getBoolean("is_custom");
                        Integer ownerId = (Integer) rs.getObject("created_by_user_id");
                        if (isCustom && (ownerId == null || ownerId != userId)) {
                            conn.rollback();
                            return false;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            String updateSql = "UPDATE problems SET title = ?, url = ?, platform_id = ?, difficulty = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, problem.getTitle());
                ps.setString(2, problem.getUrl());
                if (problem.getPlatformId() != null && problem.getPlatformId() > 0) {
                    ps.setInt(3, problem.getPlatformId());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, problem.getDifficulty());
                ps.setInt(5, problem.getId());
                ps.executeUpdate();
            }

            if (topicIds != null) {
                // Delete existing mappings
                try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM problem_topics WHERE problem_id = ?")) {
                    delPs.setInt(1, problem.getId());
                    delPs.executeUpdate();
                }
                // Insert new mappings
                if (!topicIds.isEmpty()) {
                    try (PreparedStatement insPs = conn.prepareStatement("INSERT INTO problem_topics (problem_id, topic_id) VALUES (?, ?)")) {
                        for (Integer tid : topicIds) {
                            insPs.setInt(1, problem.getId());
                            insPs.setInt(2, tid);
                            insPs.addBatch();
                        }
                        insPs.executeBatch();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public boolean deleteProblem(int problemId, int userId) throws SQLException {
        // Can only delete if user is owner of custom problem, or delete user's association if global
        String sql = "DELETE FROM problems WHERE id = ? AND is_custom = TRUE AND created_by_user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, problemId);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            if (affected > 0) return true;
        }

        // If not custom or not owner, remove user's tracking entry
        String upSql = "DELETE FROM user_problems WHERE problem_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(upSql)) {
            ps.setInt(1, problemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void updateUserProblemStatus(int userId, int problemId, String status, String notes) throws SQLException {
        String sql = "INSERT INTO user_problems (user_id, problem_id, status, notes, first_solved_at, last_attempted_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, CASE WHEN ? = 'SOLVED' THEN CURRENT_TIMESTAMP ELSE NULL END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (user_id, problem_id) DO UPDATE SET " +
                     "status = EXCLUDED.status, " +
                     "notes = COALESCE(EXCLUDED.notes, user_problems.notes), " +
                     "first_solved_at = CASE " +
                     "  WHEN EXCLUDED.status = 'SOLVED' AND user_problems.first_solved_at IS NULL THEN CURRENT_TIMESTAMP " +
                     "  ELSE user_problems.first_solved_at END, " +
                     "last_attempted_at = CURRENT_TIMESTAMP, " +
                     "updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, problemId);
            ps.setString(3, status);
            ps.setString(4, notes);
            ps.setString(5, status);
            ps.executeUpdate();
        }

        // If solved, also log submission record so streak and analytics are updated
        if ("SOLVED".equalsIgnoreCase(status) || "ATTEMPTED".equalsIgnoreCase(status)) {
            String subSql = "INSERT INTO submissions (user_id, problem_id, status, language, submitted_at) VALUES (?, ?, ?, 'Java', CURRENT_TIMESTAMP)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(subSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, problemId);
                ps.setString(3, "SOLVED".equalsIgnoreCase(status) ? "ACCEPTED" : "WRONG_ANSWER");
                ps.executeUpdate();
            }
        }
    }

    private Problem mapProblem(ResultSet rs) throws SQLException {
        Problem p = new Problem();
        p.setId(rs.getInt("id"));
        p.setTitle(rs.getString("title"));
        p.setUrl(rs.getString("url"));
        p.setPlatformId((Integer) rs.getObject("platform_id"));
        p.setPlatformName(rs.getString("platform_name"));
        p.setDifficulty(rs.getString("difficulty"));
        p.setCustom(rs.getBoolean("is_custom"));
        p.setCreatedByUserId((Integer) rs.getObject("created_by_user_id"));
        p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        p.setUserStatus(rs.getString("user_status"));
        p.setUserNotes(rs.getString("user_notes"));
        p.setFirstSolvedAt(rs.getObject("first_solved_at", OffsetDateTime.class));
        p.setLastAttemptedAt(rs.getObject("last_attempted_at", OffsetDateTime.class));
        return p;
    }
}
