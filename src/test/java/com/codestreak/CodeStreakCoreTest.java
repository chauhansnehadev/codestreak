package com.codestreak;

import com.codestreak.dao.*;
import com.codestreak.model.*;
import com.codestreak.service.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CodeStreakCoreTest {

    private final UserService userService = new UserService();
    private final ProblemService problemService = new ProblemService();
    private final RecommendationService recommendationService = new RecommendationService();
    private final RevisionService revisionService = new RevisionService();
    private final AnalyticsDao analyticsDao = new AnalyticsDao();
    private final PlatformDao platformDao = new PlatformDao();

    @Test
    @Order(1)
    public void testAuthentication() throws Exception {
        Optional<User> userOpt = userService.authenticate("demouser", "password123");
        assertTrue(userOpt.isPresent(), "Demo user should authenticate successfully");
        assertEquals("demouser", userOpt.get().getUsername());
    }

    @Test
    @Order(2)
    public void testProblemListingAndFiltering() throws Exception {
        List<Problem> problems = problemService.listUserProblems(1, null, null, null, null, null);
        assertNotNull(problems);
        assertFalse(problems.isEmpty(), "Should have seeded problems");

        // Filter by EASY
        List<Problem> easyProblems = problemService.listUserProblems(1, null, null, "EASY", null, null);
        assertNotNull(easyProblems);
        for (Problem p : easyProblems) {
            assertEquals("EASY", p.getDifficulty());
        }
    }

    @Test
    @Order(3)
    public void testRecommendationEngine() throws Exception {
        List<Recommendation> recs = recommendationService.getRecommendationsForUser(1, 5);
        assertNotNull(recs);
        assertFalse(recs.isEmpty(), "Should generate recommendations for demo user");
        for (Recommendation r : recs) {
            assertNotNull(r.getProblem());
            assertNotNull(r.getRationale());
            // Must not recommend already solved problems
            assertNotEquals("SOLVED", r.getProblem().getUserStatus());
        }
    }

    @Test
    @Order(4)
    public void testRevisionScheduler() throws Exception {
        List<Revision> revs = revisionService.getRevisions(1, "PENDING");
        assertNotNull(revs);

        // Schedule new revision
        Revision newRev = revisionService.scheduleRevision(1, 2, LocalDate.now().plusDays(5), 2, "Test revision item");
        assertNotNull(newRev);
        assertTrue(newRev.getId() > 0);

        // Update revision to completed
        boolean updated = revisionService.updateStatus(newRev.getId(), 1, "COMPLETED");
        assertTrue(updated);
    }

    @Test
    @Order(5)
    public void testAnalyticsDashboard() throws Exception {
        DashboardSummary summary = analyticsDao.getDashboardSummary(1);
        assertNotNull(summary);
        assertTrue(summary.getTotalSolved() > 0, "Demo user should have solved problems");
        assertTrue(summary.getCurrentStreak() >= 1, "Current streak should be at least 1");
        assertNotNull(summary.getWeeklyActivity());
    }

    @Test
    @Order(6)
    public void testPlatforms() throws Exception {
        List<Platform> platforms = platformDao.getAllPlatforms();
        assertNotNull(platforms);
        assertTrue(platforms.size() >= 4, "Should have 4 core platforms");
    }
}
