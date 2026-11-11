package com.codestreak.dao;

import com.codestreak.model.User;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;

public class UserDao {

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, avatar_url, target_problems_per_day, created_at, updated_at " +
                     "FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, avatar_url, target_problems_per_day, created_at, updated_at " +
                     "FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, full_name, avatar_url, target_problems_per_day, created_at, updated_at " +
                     "FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, target_problems_per_day) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setInt(5, user.getTargetProblemsPerDay() > 0 ? user.getTargetProblemsPerDay() : 3);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    user.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                }
            }
        }
        return user;
    }

    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, target_problems_per_day = ?, avatar_url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setInt(2, user.getTargetProblemsPerDay());
            ps.setString(3, user.getAvatarUrl());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setTargetProblemsPerDay(rs.getInt("target_problems_per_day"));
        user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        user.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return user;
    }
}
