package com.codestreak.dao;

import com.codestreak.model.Platform;
import com.codestreak.model.UserPlatform;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlatformDao {

    public List<Platform> getAllPlatforms() throws SQLException {
        List<Platform> list = new ArrayList<>();
        String sql = "SELECT id, name, code, base_url, icon_name, is_active, created_at FROM platforms ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Platform p = new Platform();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCode(rs.getString("code"));
                p.setBaseUrl(rs.getString("base_url"));
                p.setIconName(rs.getString("icon_name"));
                p.setActive(rs.getBoolean("is_active"));
                p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                list.add(p);
            }
        }
        return list;
    }

    public List<UserPlatform> getUserPlatforms(int userId) throws SQLException {
        List<UserPlatform> list = new ArrayList<>();
        String sql = "SELECT up.id, up.user_id, up.platform_id, p.name as platform_name, p.code as platform_code, " +
                     "up.platform_username, up.profile_url, up.problems_solved, up.rating, up.global_rank, up.last_synced, up.created_at " +
                     "FROM user_platforms up " +
                     "JOIN platforms p ON up.platform_id = p.id " +
                     "WHERE up.user_id = ? ORDER BY p.id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserPlatform up = new UserPlatform();
                    up.setId(rs.getInt("id"));
                    up.setUserId(rs.getInt("user_id"));
                    up.setPlatformId(rs.getInt("platform_id"));
                    up.setPlatformName(rs.getString("platform_name"));
                    up.setPlatformCode(rs.getString("platform_code"));
                    up.setPlatformUsername(rs.getString("platform_username"));
                    up.setProfileUrl(rs.getString("profile_url"));
                    up.setProblemsSolved(rs.getInt("problems_solved"));
                    up.setRating(rs.getInt("rating"));
                    up.setGlobalRank(rs.getString("global_rank"));
                    up.setLastSynced(rs.getObject("last_synced", OffsetDateTime.class));
                    up.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    list.add(up);
                }
            }
        }
        return list;
    }

    public void upsertUserPlatform(UserPlatform up) throws SQLException {
        String sql = "INSERT INTO user_platforms (user_id, platform_id, platform_username, profile_url, problems_solved, rating, global_rank, last_synced, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (user_id, platform_id) DO UPDATE SET " +
                     "platform_username = EXCLUDED.platform_username, " +
                     "profile_url = EXCLUDED.profile_url, " +
                     "problems_solved = EXCLUDED.problems_solved, " +
                     "rating = EXCLUDED.rating, " +
                     "global_rank = EXCLUDED.global_rank, " +
                     "last_synced = CURRENT_TIMESTAMP, " +
                     "updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, up.getUserId());
            ps.setInt(2, up.getPlatformId());
            ps.setString(3, up.getPlatformUsername());
            ps.setString(4, up.getProfileUrl());
            ps.setInt(5, up.getProblemsSolved());
            ps.setInt(6, up.getRating());
            ps.setString(7, up.getGlobalRank());
            ps.executeUpdate();
        }
    }

    public boolean deleteUserPlatform(int userId, int platformId) throws SQLException {
        String sql = "DELETE FROM user_platforms WHERE user_id = ? AND platform_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, platformId);
            return ps.executeUpdate() > 0;
        }
    }
}
