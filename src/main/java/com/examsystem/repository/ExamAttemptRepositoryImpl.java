package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.ExamAttempt;
import com.examsystem.model.ExamMonitoringEntry;
import com.examsystem.model.ExamReportEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamAttemptRepositoryImpl implements ExamAttemptRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExamAttemptRepositoryImpl.class);

    private static final String SELECT_BY_ASSIGNMENT = "SELECT * FROM exam_attempts WHERE assignment_id = ?";
    private static final String INSERT_ATTEMPT = "INSERT INTO exam_attempts (assignment_id, start_time, submission_status) VALUES (?, ?, ?)";
    private static final String UPDATE_ATTEMPT = "UPDATE exam_attempts SET end_time = ?, total_marks_obtained = ?, submission_status = ? WHERE attempt_id = ?";
    private static final String SELECT_ACTIVE_BY_TEACHER =
            "SELECT ea.attempt_id, u.full_name AS student_name, e.exam_name, ea.submission_status, ea.start_time "
                    + "FROM exam_attempts ea "
                    + "JOIN student_exam_assignments sea ON ea.assignment_id = sea.assignment_id "
                    + "JOIN students s ON sea.student_id = s.student_id "
                    + "JOIN users u ON s.user_id = u.user_id "
                    + "JOIN exams e ON sea.exam_id = e.exam_id "
                    + "WHERE e.teacher_id = ? AND ea.submission_status = 'in_progress' "
                    + "ORDER BY ea.start_time DESC";
    private static final String SELECT_REPORTS_BY_TEACHER =
            "SELECT e.exam_id, u.full_name AS student_name, e.exam_name, ea.total_marks_obtained, e.total_marks, "
                    + "ea.submission_status, ea.end_time "
                    + "FROM exam_attempts ea "
                    + "JOIN student_exam_assignments sea ON ea.assignment_id = sea.assignment_id "
                    + "JOIN students s ON sea.student_id = s.student_id "
                    + "JOIN users u ON s.user_id = u.user_id "
                    + "JOIN exams e ON sea.exam_id = e.exam_id "
                    + "WHERE e.teacher_id = ? AND ea.submission_status = 'submitted' "
                    + "ORDER BY ea.end_time DESC";
    private static final String SELECT_REPORTS_BY_TEACHER_EXAM =
            "SELECT e.exam_id, u.full_name AS student_name, e.exam_name, ea.total_marks_obtained, e.total_marks, "
                    + "ea.submission_status, ea.end_time "
                    + "FROM exam_attempts ea "
                    + "JOIN student_exam_assignments sea ON ea.assignment_id = sea.assignment_id "
                    + "JOIN students s ON sea.student_id = s.student_id "
                    + "JOIN users u ON s.user_id = u.user_id "
                    + "JOIN exams e ON sea.exam_id = e.exam_id "
                    + "WHERE e.teacher_id = ? AND e.exam_id = ? AND ea.submission_status = 'submitted' "
                    + "ORDER BY ea.end_time DESC";

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

    @Override
    public List<ExamMonitoringEntry> findActiveAttemptsByTeacherId(int teacherId) {
        List<ExamMonitoringEntry> entries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_BY_TEACHER)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamMonitoringEntry entry = new ExamMonitoringEntry();
                    entry.setAttemptId(rs.getInt("attempt_id"));
                    entry.setStudentName(rs.getString("student_name"));
                    entry.setExamName(rs.getString("exam_name"));
                    entry.setSubmissionStatus(rs.getString("submission_status"));
                    Timestamp start = rs.getTimestamp("start_time");
                    if (start != null) {
                        entry.setStartTime(start.toLocalDateTime());
                    }
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading active attempts for teacher", e);
        }
        return entries;
    }

    @Override
    public List<ExamReportEntry> findSubmittedReportsByTeacherId(int teacherId) {
        List<ExamReportEntry> entries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_REPORTS_BY_TEACHER)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamReportEntry entry = new ExamReportEntry();
                    entry.setExamId(rs.getInt("exam_id"));
                    entry.setStudentName(rs.getString("student_name"));
                    entry.setExamName(rs.getString("exam_name"));
                    entry.setMarksObtained(rs.getInt("total_marks_obtained"));
                    entry.setTotalMarks(rs.getInt("total_marks"));
                    entry.setSubmissionStatus(rs.getString("submission_status"));
                    Timestamp end = rs.getTimestamp("end_time");
                    if (end != null) {
                        entry.setEndTime(end.toLocalDateTime());
                    }
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading reports for teacher", e);
        }
        return entries;
    }

    @Override
    public List<ExamReportEntry> findSubmittedReportsByTeacherAndExam(int teacherId, int examId) {
        List<ExamReportEntry> entries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_REPORTS_BY_TEACHER_EXAM)) {
            stmt.setInt(1, teacherId);
            stmt.setInt(2, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamReportEntry entry = new ExamReportEntry();
                    entry.setExamId(rs.getInt("exam_id"));
                    entry.setStudentName(rs.getString("student_name"));
                    entry.setExamName(rs.getString("exam_name"));
                    entry.setMarksObtained(rs.getInt("total_marks_obtained"));
                    entry.setTotalMarks(rs.getInt("total_marks"));
                    entry.setSubmissionStatus(rs.getString("submission_status"));
                    Timestamp end = rs.getTimestamp("end_time");
                    if (end != null) {
                        entry.setEndTime(end.toLocalDateTime());
                    }
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading reports for teacher exam", e);
        }
        return entries;
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
