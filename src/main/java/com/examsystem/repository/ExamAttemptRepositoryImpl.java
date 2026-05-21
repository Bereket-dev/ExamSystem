package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.ExamAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class ExamAttemptRepositoryImpl implements ExamAttemptRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExamAttemptRepositoryImpl.class);

    private static final String SELECT_BY_ASSIGNMENT = "SELECT * FROM exam_attempts WHERE assignment_id = ?";
    private static final String INSERT_ATTEMPT = "INSERT INTO exam_attempts (assignment_id, start_time, submission_status) VALUES (?, ?, ?)";
    private static final String UPDATE_ATTEMPT = "UPDATE exam_attempts SET end_time = ?, total_marks_obtained = ?, submission_status = ? WHERE attempt_id = ?";

    @Override
    public Optional<ExamAttempt> findByAssignmentId(int assignmentId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ASSIGNMENT)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading exam attempt", e);
        }
        return Optional.empty();
    }

    @Override
    public ExamAttempt createAttempt(int assignmentId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_ATTEMPT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, assignmentId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, "in_progress");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ExamAttempt attempt = new ExamAttempt();
                    attempt.setAttemptId(rs.getInt(1));
                    attempt.setAssignmentId(assignmentId);
                    attempt.setStartTime(LocalDateTime.now());
                    attempt.setSubmissionStatus("in_progress");
                    return attempt;
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating exam attempt", e);
        }
        throw new RuntimeException("Unable to create exam attempt");
    }

    @Override
    public void updateAttempt(ExamAttempt attempt) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_ATTEMPT)) {
            stmt.setTimestamp(1, attempt.getEndTime() == null ? null : Timestamp.valueOf(attempt.getEndTime()));
            stmt.setInt(2, attempt.getTotalMarksObtained());
            stmt.setString(3, attempt.getSubmissionStatus());
            stmt.setInt(4, attempt.getAttemptId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating exam attempt", e);
        }
    }

    private ExamAttempt mapResultSet(ResultSet rs) throws SQLException {
        ExamAttempt attempt = new ExamAttempt();
        attempt.setAttemptId(rs.getInt("attempt_id"));
        attempt.setAssignmentId(rs.getInt("assignment_id"));
        Timestamp start = rs.getTimestamp("start_time");
        if (start != null) {
            attempt.setStartTime(start.toLocalDateTime());
        }
        Timestamp end = rs.getTimestamp("end_time");
        if (end != null) {
            attempt.setEndTime(end.toLocalDateTime());
        }
        attempt.setTotalMarksObtained(rs.getInt("total_marks_obtained"));
        attempt.setSubmissionStatus(rs.getString("submission_status"));
        return attempt;
    }
}
