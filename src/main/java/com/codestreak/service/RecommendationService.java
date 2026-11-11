package com.codestreak.service;

import com.codestreak.dao.AnalyticsDao;
import com.codestreak.dao.ProblemDao;
import com.codestreak.dao.TopicDao;
import com.codestreak.model.Problem;
import com.codestreak.model.Recommendation;
import com.codestreak.model.Topic;

import java.sql.SQLException;
import java.util.*;

/**
 * Recommendation Service implementing in-memory DSA recommendation algorithms.
 * Uses HashMap, HashSet, ArrayList and PriorityQueue.
 *
 * Algorithm Strategy:
 * 1. Compute Topic Weakness Score:
 *    - Weakness is driven by:
 *      (attempted_unsolved * 3.0 + revision_needed * 2.5 + (total - solved) * 0.5) / (total + 1)
 * 2. Rank topics using PriorityQueue (highest weakness first).
 * 3. Filter problems:
 *    - Exclude problems already marked SOLVED by the user.
 *    - Select unsolved problems matching user's weak topics.
 * 4. PriorityQueue of Recommendations:
 *    - Score = TopicWeakness * 10 + DifficultyWeight (Medium: 5, Easy: 3, Hard: 4)
 *    - Top N recommendations returned with explanatory rationale.
 */
public class RecommendationService {

    private final AnalyticsDao analyticsDao = new AnalyticsDao();
    private final ProblemDao problemDao = new ProblemDao();
    private final TopicDao topicDao = new TopicDao();

    public List<Recommendation> getRecommendationsForUser(int userId, int limit) throws SQLException {
        if (limit <= 0) limit = 6;

        // 1. Fetch topic performance
        List<Map<String, Object>> topicStats = analyticsDao.getTopicPerformance(userId);

        // Map topicId -> weakness score
        Map<Integer, Double> topicWeaknessMap = new HashMap<>();
        Map<Integer, String> topicNameMap = new HashMap<>();

        for (Map<String, Object> stat : topicStats) {
            int tid = (Integer) stat.get("topicId");
            String tname = (String) stat.get("topicName");
            int total = (Integer) stat.get("totalProblems");
            int solved = (Integer) stat.get("solvedCount");
            int attempted = (Integer) stat.get("attemptedCount");
            int revision = (Integer) stat.get("revisionCount");

            topicNameMap.put(tid, tname);

            // Compute topic weakness
            // Attempted without solving indicates high struggle.
            // Revision needed indicates forgetting / concept gap.
            // Low solved ratio indicates unpracticed domain.
            double unsolved = Math.max(0, total - solved);
            double struggleRatio = (attempted * 3.0 + revision * 2.5 + unsolved * 0.8) / Math.max(1, total);
            topicWeaknessMap.put(tid, struggleRatio);
        }

        // 2. Fetch all user problems and identify solved problems
        List<Problem> allProblems = problemDao.findUserProblems(userId, null, null, null, null, null);
        Set<Integer> solvedProblemIds = new HashSet<>();

        for (Problem p : allProblems) {
            if ("SOLVED".equalsIgnoreCase(p.getUserStatus())) {
                solvedProblemIds.add(p.getId());
            }
        }

        // 3. PriorityQueue to rank candidate recommendations
        PriorityQueue<ScoredCandidate> pq = new PriorityQueue<>((a, b) -> Double.compare(b.finalScore, a.finalScore));

        for (Problem p : allProblems) {
            if (solvedProblemIds.contains(p.getId())) {
                continue; // Do not recommend already solved problems
            }

            // Determine problem difficulty score
            double diffWeight = 3.0;
            if ("MEDIUM".equalsIgnoreCase(p.getDifficulty())) diffWeight = 5.0;
            else if ("HARD".equalsIgnoreCase(p.getDifficulty())) diffWeight = 4.0; // Medium preferred for mastery

            // Boost score if problem is in REVISION_NEEDED or ATTEMPTED
            double statusBonus = 0.0;
            if ("REVISION_NEEDED".equalsIgnoreCase(p.getUserStatus())) {
                statusBonus = 8.0;
            } else if ("ATTEMPTED".equalsIgnoreCase(p.getUserStatus())) {
                statusBonus = 6.0;
            }

            // Find primary topic weakness
            double maxTopicWeakness = 0.5;
            Topic primaryTopic = null;
            if (p.getTopics() != null && !p.getTopics().isEmpty()) {
                for (Topic t : p.getTopics()) {
                    double w = topicWeaknessMap.getOrDefault(t.getId(), 0.5);
                    if (w > maxTopicWeakness || primaryTopic == null) {
                        maxTopicWeakness = w;
                        primaryTopic = t;
                    }
                }
            }

            double totalScore = (maxTopicWeakness * 10.0) + diffWeight + statusBonus;

            String rationale;
            String priorityLabel;
            if ("REVISION_NEEDED".equalsIgnoreCase(p.getUserStatus())) {
                rationale = "Flagged for revision. Re-attempt to solidify core patterns.";
                priorityLabel = "HIGH";
            } else if ("ATTEMPTED".equalsIgnoreCase(p.getUserStatus())) {
                rationale = "You previously attempted this problem. Finishing it will boost your confidence.";
                priorityLabel = "HIGH";
            } else if (maxTopicWeakness > 1.5) {
                rationale = "Targeted for " + (primaryTopic != null ? primaryTopic.getName() : "DSA") +
                            ", which is currently among your lowest-completion topics.";
                priorityLabel = "HIGH";
            } else if ("MEDIUM".equalsIgnoreCase(p.getDifficulty())) {
                rationale = "Optimal standard interview question to bridge easy concepts to advanced ones.";
                priorityLabel = "MEDIUM";
            } else {
                rationale = "Recommended foundational problem to reinforce topic mastery.";
                priorityLabel = "LOW";
            }

            pq.offer(new ScoredCandidate(p, primaryTopic, totalScore, rationale, priorityLabel));
        }

        List<Recommendation> recommendations = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < limit) {
            ScoredCandidate sc = pq.poll();
            recommendations.add(new Recommendation(sc.problem, sc.topic, sc.finalScore, sc.rationale, sc.priorityLabel));
            count++;
        }

        return recommendations;
    }

    private static class ScoredCandidate {
        Problem problem;
        Topic topic;
        double finalScore;
        String rationale;
        String priorityLabel;

        ScoredCandidate(Problem problem, Topic topic, double finalScore, String rationale, String priorityLabel) {
            this.problem = problem;
            this.topic = topic;
            this.finalScore = finalScore;
            this.rationale = rationale;
            this.priorityLabel = priorityLabel;
        }
    }
}
