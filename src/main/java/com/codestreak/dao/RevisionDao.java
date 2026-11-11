package com.codestreak.dao;

import com.codestreak.model.Revision;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class RevisionDao {

    public List<Revision> getRevisionsForUser(int userId, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT r.id, r.user_id, r.problem_id, p.title as problem_title, p.url as problem_url, " +
            "p.difficulty as problem_difficulty, r.scheduled_for, r.status, r.priority, r.notes, " +
            "r.completed_at, r.created_at " +
            "FROM revisions r " +
            "JOIN problems p ON r.problem_id = p.id " +
            "WHERE r.user_id = ? "
        );

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            if ("OVERDUE".equalsIgnoreCase(statusFilter)) {
                sql.append("AND r.status = 'PENDING' AND r.scheduled_for < CURRENT_DATE ");
            } else if ("UPCOMING".equalsIgnoreCase(statusFilter)) {
                sql.append("AND r.status = 'PENDING' AND r.scheduled_for >= CURRENT_DATE ");
            } else {
                sql.append("AND r.status = ? ");
            }
        }

        sql.append("ORDER BY r.scheduled_for ASC, r.priority DESC");

        List<Revision> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (statusFilter != null && !statusFilter.trim().isEmpty() &&
                !"ALL".equalsIgnoreCase(statusFilter) &&
                !"OVERDUE".equalsIgnoreCase(statusFilter) &&
                !"UPCOMING".equalsIgnoreCase(statusFilter)) {
                ps.setString(2, statusFilter.toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRevision(rs));
                }
            }
        }
        return list;
    }

    public Revision createRevision(Revision r) throws SQLException {
        String sql = "INSERT INTO revisions (user_id, problem_id, scheduled_for, status, priority, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getProblemId());
            ps.setDate(3, Date.valueOf(r.getScheduledFor()));
            ps.setString(4, r.getStatus() != null ? r.getStatus() : "PENDING");
            ps.setInt(5, r.getPriority() > 0 ? r.getPriority() : 1);
            ps.setString(6, r.getNotes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setId(rs.getInt("id"));
                    r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                }
            }
        }
        return r;
    }

    public boolean updateRevisionStatus(int revisionId, int userId, String status) throws SQLException {
        String sql = "UPDATE revisions SET status = ?, " +
                     "completed_at = CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE NULL END " +
                     "WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, revisionId);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteRevision(int revisionId, int userId) throws SQLException {
        String sql = "DELETE FROM revisions WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, revisionId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private Revision mapRevision(ResultSet rs) throws SQLException {
        Revision r = new Revision();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setProblemId(rs.getInt("problem_id"));
        r.setProblemTitle(rs.getString("problem_title"));
        r.setProblemUrl(rs.getString("problem_url"));
        r.setProblemDifficulty(rs.getString("problem_difficulty"));
        r.setScheduledFor(rs.getDate("scheduled_for").toLocalDate());
        r.setStatus(rs.getString("status"));
        r.setPriority(rs.getInt("priority"));
        r.setNotes(rs.getString("notes"));
        r.setCompletedAt(rs.getObject("completed_at", OffsetDateTime.class));
        r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return r;
    }
}
