package com.codestreak.service;

import com.codestreak.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformFetcherService {

    private final HttpClient httpClient;

    public PlatformFetcherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Extracts the pure username from a raw username or full profile URL.
     */
    public String extractUsername(String platformCode, String input) {
        if (input == null || input.isBlank()) return "";
        input = input.trim();

        if (platformCode == null) return input;

        switch (platformCode.toLowerCase()) {
            case "leetcode": {
                // e.g. https://leetcode.com/u/neal_wu/ or https://leetcode.com/neal_wu/
                Pattern pattern = Pattern.compile("leetcode\\.com/(?:u/)?([a-zA-Z0-9_-]+)/?");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                return input.replaceAll("[^a-zA-Z0-9_-]", "");
            }
            case "codeforces": {
                // e.g. https://codeforces.com/profile/tourist
                Pattern pattern = Pattern.compile("codeforces\\.com/profile/([a-zA-Z0-9_-]+)/?");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                return input.replaceAll("[^a-zA-Z0-9_.-]", "");
            }
            case "hackerrank": {
                // e.g. https://www.hackerrank.com/profile/harsh or https://www.hackerrank.com/harsh
                Pattern pattern = Pattern.compile("hackerrank\\.com/(?:profile/)?([a-zA-Z0-9_.-]+)/?");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                return input.replaceAll("[^a-zA-Z0-9_.-]", "");
            }
            default:
                return input;
        }
    }

    /**
     * Fetches live statistics from the specified platform.
     */
    public Map<String, Object> fetchProfileStats(String platformCode, String input) throws Exception {
        String username = extractUsername(platformCode, input);
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username or profile link is empty.");
        }

        switch (platformCode.toLowerCase()) {
            case "leetcode":
                return fetchLeetCode(username);
            case "codeforces":
                return fetchCodeforces(username);
            case "hackerrank":
                return fetchHackerRank(username);
            default:
                throw new UnsupportedOperationException("Automated fetching is not available for " + platformCode + ". Please enter details manually.");
        }
    }

    private Map<String, Object> fetchLeetCode(String username) throws Exception {
        String graphqlQuery = "{"
                + "\"query\": \"query getUserProfile($username: String!) { matchedUser(username: $username) { submitStats: submitStatsGlobal { acSubmissionNum { difficulty count } } profile { ranking } } userContestRanking(username: $username) { rating globalRanking } }\","
                + "\"variables\": {\"username\": \"" + username + "\"}"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                .header("Referer", "https://leetcode.com")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(graphqlQuery))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("LeetCode returned HTTP " + response.statusCode());
        }

        JsonNode root = JsonUtil.getMapper().readTree(response.body());
        JsonNode data = root.path("data");
        JsonNode matchedUser = data.path("matchedUser");

        if (matchedUser.isMissingNode() || matchedUser.isNull()) {
            throw new IllegalArgumentException("LeetCode user '" + username + "' not found.");
        }

        int problemsSolved = 0;
        JsonNode acSubmissions = matchedUser.path("submitStats").path("acSubmissionNum");
        if (acSubmissions.isArray()) {
            for (JsonNode sub : acSubmissions) {
                if ("All".equalsIgnoreCase(sub.path("difficulty").asText())) {
                    problemsSolved = sub.path("count").asInt(0);
                    break;
                }
            }
        }

        int contestRating = 0;
        JsonNode contestRanking = data.path("userContestRanking");
        if (!contestRanking.isMissingNode() && !contestRanking.isNull()) {
            contestRating = (int) Math.round(contestRanking.path("rating").asDouble(0.0));
        }

        String globalRank = null;
        int profileRank = matchedUser.path("profile").path("ranking").asInt(0);
        if (profileRank > 0) {
            globalRank = "Top #" + String.format("%,d", profileRank);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("profileUrl", "https://leetcode.com/u/" + username + "/");
        result.put("problemsSolved", problemsSolved);
        result.put("rating", contestRating);
        result.put("globalRank", globalRank);
        return result;
    }

    private Map<String, Object> fetchCodeforces(String username) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://codeforces.com/api/user.info?handles=" + username))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Codeforces returned HTTP " + response.statusCode());
        }

        JsonNode root = JsonUtil.getMapper().readTree(response.body());
        String status = root.path("status").asText();
        if (!"OK".equalsIgnoreCase(status)) {
            String comment = root.path("comment").asText("User not found.");
            throw new IllegalArgumentException("Codeforces error: " + comment);
        }

        JsonNode results = root.path("result");
        if (!results.isArray() || results.isEmpty()) {
            throw new IllegalArgumentException("Codeforces user '" + username + "' not found.");
        }

        JsonNode userNode = results.get(0);
        int rating = userNode.path("rating").asInt(0);
        String rank = userNode.path("rank").asText(null);
        if (rank != null && !rank.isBlank()) {
            rank = Character.toUpperCase(rank.charAt(0)) + rank.substring(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("username", userNode.path("handle").asText(username));
        result.put("profileUrl", "https://codeforces.com/profile/" + username);
        result.put("problemsSolved", 0);
        result.put("rating", rating);
        result.put("globalRank", rank);
        return result;
    }

    private Map<String, Object> fetchHackerRank(String username) throws Exception {
        // 1. Fetch main profile
        HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.hackerrank.com/rest/hackers/" + username))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> profileRes = httpClient.send(profileReq, HttpResponse.BodyHandlers.ofString());
        if (profileRes.statusCode() == 404 || profileRes.body().contains("\"error\":\"Not Found\"")) {
            throw new IllegalArgumentException("HackerRank user '" + username + "' not found.");
        }
        if (profileRes.statusCode() != 200) {
            throw new RuntimeException("HackerRank returned HTTP " + profileRes.statusCode());
        }

        // 2. Fetch badges for solved challenges & stars
        HttpRequest badgesReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.hackerrank.com/rest/hackers/" + username + "/badges"))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> badgesRes = httpClient.send(badgesReq, HttpResponse.BodyHandlers.ofString());

        int totalSolved = 0;
        int totalScore = 0;
        String topBadge = null;

        if (badgesRes.statusCode() == 200) {
            JsonNode badgesRoot = JsonUtil.getMapper().readTree(badgesRes.body());
            JsonNode models = badgesRoot.path("models");
            if (models.isArray()) {
                int maxStars = 0;
                for (JsonNode badge : models) {
                    int solved = badge.path("solved").asInt(0);
                    int stars = badge.path("stars").asInt(0);
                    double currentPoints = badge.path("current_points").asDouble(0);
                    totalSolved += solved;
                    totalScore += (int) currentPoints;

                    String badgeName = badge.path("badge_name").asText("");
                    if (stars > maxStars || (stars == maxStars && topBadge == null)) {
                        maxStars = stars;
                        topBadge = stars + "★ " + badgeName;
                    }
                }
            }
        }

        if (topBadge == null) {
            topBadge = "HackerRank Member";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("profileUrl", "https://www.hackerrank.com/profile/" + username);
        result.put("problemsSolved", totalSolved);
        result.put("rating", totalScore);
        result.put("globalRank", topBadge);
        return result;
    }
}
