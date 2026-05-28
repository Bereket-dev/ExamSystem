package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.Exam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamRepositoryImpl implements ExamRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExamRepositoryImpl.class);

    private static final String INSERT_EXAM = "INSERT INTO exams (teacher_id, exam_name, description, subject, course_id, duration_minutes, total_questions, total_marks, passing_marks, exam_date, exam_time, is_published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM exams WHERE exam_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM exams ORDER BY exam_id";
    private static final String SELECT_BY_TEACHER = "SELECT * FROM exams WHERE teacher_id = ? ORDER BY exam_date";
    private static final String SELECT_PUBLISHED = "SELECT * FROM exams WHERE is_published = TRUE ORDER BY exam_date";
    private static final String UPDATE_EXAM = "UPDATE exams SET teacher_id = ?, exam_name = ?, description = ?, subject = ?, course_id = ?, duration_minutes = ?, total_questions = ?, total_marks = ?, passing_marks = ?, exam_date = ?, exam_time = ?, is_published = ? WHERE exam_id = ?";
    private static final String DELETE_EXAM = "DELETE FROM exams WHERE exam_id = ?";
    private static final String SET_PUBLISHED = "UPDATE exams SET is_published = ? WHERE exam_id = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) as count FROM exams";

    @Override
    public void save(Exam exam) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_EXAM, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, exam.getTeacherId());
            stmt.setString(2, exam.getExamName());
            stmt.setString(3, exam.getDescription());
            stmt.setString(4, exam.getSubject());
            stmt.setInt(5, exam.getCourseId());
            stmt.setInt(6, exam.getDurationMinutes());
            stmt.setInt(7, exam.getTotalQuestions());
            stmt.setInt(8, exam.getTotalMarks());
            stmt.setInt(9, exam.getPassingMarks());
            stmt.setDate(10, exam.getExamDate() == null ? null : Date.valueOf(exam.getExamDate()));
            stmt.setTime(11, exam.getExamTime() == null ? null : Time.valueOf(exam.getExamTime()));
            stmt.setBoolean(12, exam.isPublished());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        exam.setExamId(rs.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving exam", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Exam> findById(int examId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToExam(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding exam by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Exam> findAll() {
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                list.add(mapRowToExam(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all exams", e);
        }
        return list;
    }

    @Override
    public List<Exam> findByTeacherId(int teacherId) {
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TEACHER)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExam(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding exams by teacher", e);
        }
        return list;
    }

    @Override
    public List<Exam> findPublished() {
        List<Exam> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_PUBLISHED)) {
            while (rs.next()) {
                list.add(mapRowToExam(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding published exams", e);
        }
        return list;
    }

    @Override
    public void update(Exam exam) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_EXAM)) {

            stmt.setInt(1, exam.getTeacherId());
            stmt.setString(2, exam.getExamName());
            stmt.setString(3, exam.getDescription());
            stmt.setString(4, exam.getSubject());
            stmt.setInt(5, exam.getCourseId());
            stmt.setInt(6, exam.getDurationMinutes());
            stmt.setInt(7, exam.getTotalQuestions());
            stmt.setInt(8, exam.getTotalMarks());
            stmt.setInt(9, exam.getPassingMarks());
            stmt.setDate(10, exam.getExamDate() == null ? null : Date.valueOf(exam.getExamDate()));
            stmt.setTime(11, exam.getExamTime() == null ? null : Time.valueOf(exam.getExamTime()));
            stmt.setBoolean(12, exam.isPublished());
            stmt.setInt(13, exam.getExamId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating exam", e);
        }
    }

    @Override
    public void delete(int examId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_EXAM)) {
            stmt.setInt(1, examId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting exam", e);
        }
    }

    @Override
    public void setPublished(int examId, boolean published) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SET_PUBLISHED)) {
            stmt.setBoolean(1, published);
            stmt.setInt(2, examId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting published", e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(COUNT_ALL)) {
            if (rs.next()) {
                return rs.getLong("count");
            }
        } catch (SQLException e) {
            logger.error("Error counting exams", e);
        }
        return 0;
    }

    private Exam mapRowToExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setTeacherId(rs.getInt("teacher_id"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setDescription(rs.getString("description"));
        exam.setSubject(rs.getString("subject"));
        exam.setCourseId(rs.getInt("course_id"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setTotalQuestions(rs.getInt("total_questions"));
        exam.setTotalMarks(rs.getInt("total_marks"));
        exam.setPassingMarks(rs.getInt("passing_marks"));
        Date d = rs.getDate("exam_date");
        Time t = rs.getTime("exam_time");
        if (d != null)
            exam.setExamDate(d.toLocalDate());
        if (t != null)
            exam.setExamTime(t.toLocalTime());
        exam.setPublished(rs.getBoolean("is_published"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null)
            exam.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null)
            exam.setUpdatedAt(updated.toLocalDateTime());
        return exam;
    }
}
