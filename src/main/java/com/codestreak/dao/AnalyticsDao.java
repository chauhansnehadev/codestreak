package com.codestreak.dao;

import com.codestreak.model.DashboardSummary;
import com.codestreak.model.Submission;
import com.codestreak.model.UserPlatform;
import com.codestreak.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class AnalyticsDao {

    public DashboardSummary getDashboardSummary(int userId) throws SQLException {
        DashboardSummary summary = new DashboardSummary();

        // 1. Problems count & difficulty distribution
        String countsSql = "SELECT " +
            "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' THEN p.id END) as solved_count, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'ATTEMPTED' THEN p.id END) as attempted_count, " +
            "COUNT(DISTINCT p.id) as total_problems, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' AND p.difficulty = 'EASY' THEN p.id END) as easy_count, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' AND p.difficulty = 'MEDIUM' THEN p.id END) as medium_count, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' AND p.difficulty = 'HARD' THEN p.id END) as hard_count " +
            "FROM problems p " +
            "LEFT JOIN user_problems up ON p.id = up.problem_id AND up.user_id = ? " +
            "WHERE p.is_custom = FALSE OR p.created_by_user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(countsSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setTotalSolved(rs.getInt("solved_count"));
                    summary.setTotalAttempted(rs.getInt("attempted_count"));
                    summary.setTotalProblems(rs.getInt("total_problems"));
                    summary.setEasySolved(rs.getInt("easy_count"));
                    summary.setMediumSolved(rs.getInt("medium_count"));
                    summary.setHardSolved(rs.getInt("hard_count"));
                }
            }
        }

        // 2. Pending Revisions
        String revSql = "SELECT COUNT(*) FROM revisions WHERE user_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(revSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setPendingRevisions(rs.getInt(1));
                }
            }
        }

        // 3. Platform Solved Breakdown from logged problems
        String platformSql = "SELECT COALESCE(pl.name, 'Other') as platform_name, COUNT(DISTINCT up.problem_id) as cnt " +
            "FROM user_problems up " +
            "JOIN problems p ON up.problem_id = p.id " +
            "LEFT JOIN platforms pl ON p.platform_id = pl.id " +
            "WHERE up.user_id = ? AND up.status = 'SOLVED' " +
            "GROUP BY pl.name";
        Map<String, Integer> platformMap = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(platformSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    platformMap.put(rs.getString("platform_name"), rs.getInt("cnt"));
                }
            }
        }

        // 3b. Read live connected platforms from user_platforms
        String upSql = "SELECT up.id, up.user_id, up.platform_id, p.name as platform_name, p.code as platform_code, " +
                       "up.platform_username, up.profile_url, up.problems_solved, up.rating, up.global_rank " +
                       "FROM user_platforms up " +
                       "JOIN platforms p ON up.platform_id = p.id " +
                       "WHERE up.user_id = ? ORDER BY p.id ASC";
        List<UserPlatform> connectedList = new ArrayList<>();
        int externalSolvedTotal = 0;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(upSql)) {
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
                    int solved = rs.getInt("problems_solved");
                    up.setProblemsSolved(solved);
                    up.setRating(rs.getInt("rating"));
                    up.setGlobalRank(rs.getString("global_rank"));
                    connectedList.add(up);

                    externalSolvedTotal += solved;

                    // If connected platform has solves, sync with platformMap
                    String pName = rs.getString("platform_name");
                    if (solved > 0) {
                        int existing = platformMap.getOrDefault(pName, 0);
                        platformMap.put(pName, Math.max(existing, solved));
                    }
                }
            }
        }
        summary.setConnectedPlatforms(connectedList);
        summary.setExternalTotalSolved(externalSolvedTotal);
        summary.setPlatformSolved(platformMap);

        // If user has connected profiles with solves, synchronize the primary total solved
        if (externalSolvedTotal > 0) {
            summary.setTotalSolved(Math.max(summary.getTotalSolved(), externalSolvedTotal));
        }

        // 4. Topic Solved Breakdown
        String topicSql = "SELECT t.name as topic_name, COUNT(DISTINCT up.problem_id) as cnt " +
            "FROM user_problems up " +
            "JOIN problem_topics pt ON up.problem_id = pt.problem_id " +
            "JOIN topics t ON pt.topic_id = t.id " +
            "WHERE up.user_id = ? AND up.status = 'SOLVED' " +
            "GROUP BY t.name ORDER BY cnt DESC";
        Map<String, Integer> topicMap = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(topicSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topicMap.put(rs.getString("topic_name"), rs.getInt("cnt"));
                }
            }
        }
        summary.setTopicSolved(topicMap);

        // 5. Activity Dates and Streak Calculation
        String actSql = "SELECT DATE(submitted_at) as sub_date, COUNT(*) as cnt " +
            "FROM submissions WHERE user_id = ? " +
            "GROUP BY DATE(submitted_at) ORDER BY sub_date DESC";
        Map<String, Integer> weeklyActivity = new LinkedHashMap<>();
        Set<LocalDate> submissionDates = new TreeSet<>(Collections.reverseOrder());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(actSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate("sub_date").toLocalDate();
                    submissionDates.add(d);
                    weeklyActivity.put(d.toString(), rs.getInt("cnt"));
                }
            }
        }
        summary.setWeeklyActivity(weeklyActivity);

        // Calculate Streak
        int currentStreak = 0;
        int longestStreak = 0;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        if (submissionDates.contains(today)) {
            currentStreak++;
            checkDate = today.minusDays(1);
        } else if (submissionDates.contains(today.minusDays(1))) {
            checkDate = today.minusDays(1);
        } else {
            checkDate = null;
        }

        if (checkDate != null) {
            while (submissionDates.contains(checkDate)) {
                if (!checkDate.equals(today)) {
                    currentStreak++;
                }
                checkDate = checkDate.minusDays(1);
            }
        }

        // Longest Streak
        int runningStreak = 0;
        LocalDate prev = null;
        List<LocalDate> sortedDates = new ArrayList<>(submissionDates);
        Collections.sort(sortedDates); // chronological
        for (LocalDate d : sortedDates) {
            if (prev == null || prev.plusDays(1).equals(d)) {
                runningStreak++;
            } else if (!prev.equals(d)) {
                runningStreak = 1;
            }
            if (runningStreak > longestStreak) {
                longestStreak = runningStreak;
            }
            prev = d;
        }

        summary.setCurrentStreak(currentStreak);
        summary.setLongestStreak(Math.max(currentStreak, longestStreak));

        return summary;
    }

    public List<Submission> getRecentSubmissions(int userId, int limit) throws SQLException {
        String sql = "SELECT s.id, s.user_id, s.problem_id, p.title as problem_title, s.status, s.language, " +
                     "s.runtime_ms, s.memory_kb, s.notes, s.submitted_at " +
                     "FROM submissions s " +
                     "JOIN problems p ON s.problem_id = p.id " +
                     "WHERE s.user_id = ? " +
                     "ORDER BY s.submitted_at DESC LIMIT ?";
        List<Submission> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Submission s = new Submission();
                    s.setId(rs.getInt("id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setProblemId(rs.getInt("problem_id"));
                    s.setProblemTitle(rs.getString("problem_title"));
                    s.setStatus(rs.getString("status"));
                    s.setLanguage(rs.getString("language"));
                    s.setRuntimeMs((Integer) rs.getObject("runtime_ms"));
                    s.setMemoryKb((Integer) rs.getObject("memory_kb"));
                    s.setNotes(rs.getString("notes"));
                    s.setSubmittedAt(rs.getObject("submitted_at", OffsetDateTime.class));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getTopicPerformance(int userId) throws SQLException {
        String sql = "SELECT t.id, t.name, t.slug, " +
            "COUNT(DISTINCT pt.problem_id) as total_problems, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'SOLVED' THEN pt.problem_id END) as solved_count, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'ATTEMPTED' THEN pt.problem_id END) as attempted_count, " +
            "COUNT(DISTINCT CASE WHEN up.status = 'REVISION_NEEDED' THEN pt.problem_id END) as revision_count " +
            "FROM topics t " +
            "LEFT JOIN problem_topics pt ON t.id = pt.topic_id " +
            "LEFT JOIN user_problems up ON pt.problem_id = up.problem_id AND up.user_id = ? " +
            "GROUP BY t.id, t.name, t.slug, t.display_order " +
            "ORDER BY t.display_order ASC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("topicId", rs.getInt("id"));
                    map.put("topicName", rs.getString("name"));
                    map.put("topicSlug", rs.getString("slug"));
                    int total = rs.getInt("total_problems");
                    int solved = rs.getInt("solved_count");
                    int attempted = rs.getInt("attempted_count");
                    int revision = rs.getInt("revision_count");
                    map.put("totalProblems", total);
                    map.put("solvedCount", solved);
                    map.put("attemptedCount", attempted);
                    map.put("revisionCount", revision);
                    double completionPct = total > 0 ? ((double) solved / total) * 100.0 : 0.0;
                    map.put("completionPercentage", Math.round(completionPct * 10.0) / 10.0);
                    result.add(map);
                }
            }
        }
        return result;
    }
}
