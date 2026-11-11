package com.codestreak.service;

import com.codestreak.dao.ProblemDao;
import com.codestreak.dao.RevisionDao;
import com.codestreak.model.Problem;
import com.codestreak.model.Revision;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RevisionService {

    private final RevisionDao revisionDao = new RevisionDao();
    private final ProblemDao problemDao = new ProblemDao();

    public List<Revision> getRevisions(int userId, String statusFilter) throws SQLException {
        return revisionDao.getRevisionsForUser(userId, statusFilter);
    }

    public Revision scheduleRevision(int userId, int problemId, LocalDate scheduledFor, int priority, String notes) throws Exception {
        if (scheduledFor == null) {
            scheduledFor = LocalDate.now().plusDays(3); // Default spaced interval 3 days
        }
        Revision r = new Revision();
        r.setUserId(userId);
        r.setProblemId(problemId);
        r.setScheduledFor(scheduledFor);
        r.setStatus("PENDING");
        r.setPriority(priority > 0 ? priority : 1);
        r.setNotes(notes);

        return revisionDao.createRevision(r);
    }

    public boolean updateStatus(int revisionId, int userId, String status) throws SQLException {
        if (status == null || (!status.equalsIgnoreCase("PENDING") &&
            !status.equalsIgnoreCase("COMPLETED") &&
            !status.equalsIgnoreCase("SKIPPED"))) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return revisionDao.updateRevisionStatus(revisionId, userId, status.toUpperCase());
    }

    public boolean deleteRevision(int revisionId, int userId) throws SQLException {
        return revisionDao.deleteRevision(revisionId, userId);
    }

    /**
     * Spaced repetition generator: finds solved problems older than 7 days or
     * marked REVISION_NEEDED that have no pending revisions, and returns prioritized suggestions.
     */
    public List<Problem> getRevisionSuggestions(int userId, int limit) throws SQLException {
        List<Problem> userProblems = problemDao.findUserProblems(userId, null, null, null, null, null);
        List<Revision> currentRevisions = revisionDao.getRevisionsForUser(userId, "PENDING");

        Set<Integer> alreadyScheduled = new HashSet<>();
        for (Revision rev : currentRevisions) {
            alreadyScheduled.add(rev.getProblemId());
        }

        // PriorityQueue to rank revision candidates: REVISION_NEEDED first, then solved problems
        PriorityQueue<Problem> pq = new PriorityQueue<>((p1, p2) -> {
            boolean r1 = "REVISION_NEEDED".equalsIgnoreCase(p1.getUserStatus());
            boolean r2 = "REVISION_NEEDED".equalsIgnoreCase(p2.getUserStatus());
            if (r1 && !r2) return -1;
            if (!r1 && r2) return 1;
            return Integer.compare(p1.getId(), p2.getId());
        });

        for (Problem p : userProblems) {
            if (alreadyScheduled.contains(p.getId())) continue;
            if ("REVISION_NEEDED".equalsIgnoreCase(p.getUserStatus()) || "SOLVED".equalsIgnoreCase(p.getUserStatus())) {
                pq.offer(p);
            }
        }

        List<Problem> suggestions = new ArrayList<>();
        int count = 0;
        int max = limit > 0 ? limit : 5;
        while (!pq.isEmpty() && count < max) {
            suggestions.add(pq.poll());
            count++;
        }
        return suggestions;
    }
}
