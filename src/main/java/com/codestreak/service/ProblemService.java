package com.codestreak.service;

import com.codestreak.dao.ProblemDao;
import com.codestreak.model.Problem;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProblemService {

    private final ProblemDao problemDao = new ProblemDao();

    public List<Problem> listUserProblems(int userId, String search, String status, String difficulty, Integer topicId, Integer platformId) throws SQLException {
        return problemDao.findUserProblems(userId, search, status, difficulty, topicId, platformId);
    }

    public Optional<Problem> getProblem(int problemId, int userId) throws SQLException {
        return problemDao.findById(problemId, userId);
    }

    public Problem createProblem(Problem problem, List<Integer> topicIds, int userId) throws Exception {
        if (problem.getTitle() == null || problem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Problem title is required.");
        }
        if (problem.getDifficulty() == null || (!problem.getDifficulty().equalsIgnoreCase("EASY") &&
            !problem.getDifficulty().equalsIgnoreCase("MEDIUM") &&
            !problem.getDifficulty().equalsIgnoreCase("HARD"))) {
            problem.setDifficulty("MEDIUM");
        } else {
            problem.setDifficulty(problem.getDifficulty().toUpperCase());
        }

        problem.setCustom(true);
        problem.setCreatedByUserId(userId);
        Problem created = problemDao.createProblem(problem, topicIds);

        // Record initial user status if provided
        String status = problem.getUserStatus() != null ? problem.getUserStatus() : "NOT_STARTED";
        problemDao.updateUserProblemStatus(userId, created.getId(), status, problem.getUserNotes());

        return created;
    }

    public boolean updateProblem(Problem problem, List<Integer> topicIds, int userId) throws SQLException {
        return problemDao.updateProblem(problem, topicIds, userId);
    }

    public boolean deleteProblem(int problemId, int userId) throws SQLException {
        return problemDao.deleteProblem(problemId, userId);
    }

    public void updateStatus(int userId, int problemId, String status, String notes) throws SQLException {
        if (status == null || (!status.equalsIgnoreCase("NOT_STARTED") &&
            !status.equalsIgnoreCase("ATTEMPTED") &&
            !status.equalsIgnoreCase("SOLVED") &&
            !status.equalsIgnoreCase("REVISION_NEEDED"))) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        problemDao.updateUserProblemStatus(userId, problemId, status.toUpperCase(), notes);
    }
}
