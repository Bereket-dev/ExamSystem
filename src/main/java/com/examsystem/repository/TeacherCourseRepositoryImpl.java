package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.TeacherCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Implementation of TeacherCourseRepository for MySQL database.
 * Handles teacher-course assignment operations.
 */
public class TeacherCourseRepositoryImpl implements TeacherCourseRepository {
    private static final Logger logger = LoggerFactory.getLogger(TeacherCourseRepositoryImpl.class);

    private static final String INSERT_ASSIGNMENT = "INSERT INTO teacher_courses (teacher_id, course_id, is_active) VALUES (?, ?, ?) "
            +
            "ON DUPLICATE KEY UPDATE is_active = VALUES(is_active), removed_date = NULL, assigned_date = CURRENT_TIMESTAMP";

    private static final String SELECT_BY_ID = "SELECT * FROM teacher_courses WHERE teacher_course_id = ?";

    private static final String SELECT_BY_TEACHER = "SELECT * FROM teacher_courses WHERE teacher_id = ? AND is_active = TRUE ORDER BY assigned_date";

    private static final String SELECT_BY_COURSE = "SELECT * FROM teacher_courses WHERE course_id = ? AND is_active = TRUE ORDER BY assigned_date";

    private static final String SELECT_ALL_ACTIVE = "SELECT * FROM teacher_courses WHERE is_active = TRUE ORDER BY teacher_id, course_id";

    private static final String DELETE_ASSIGNMENT = "DELETE FROM teacher_courses WHERE teacher_course_id = ?";

    private static final String REMOVE_TEACHER_FROM_COURSE = "UPDATE teacher_courses SET is_active = FALSE, removed_date = NOW() WHERE teacher_id = ? AND course_id = ?";

    private static final String CHECK_ASSIGNMENT = "SELECT COUNT(*) as count FROM teacher_courses WHERE teacher_id = ? AND course_id = ? AND is_active = TRUE";

    private static final String COUNT_ALL = "SELECT COUNT(*) as count FROM teacher_courses WHERE is_active = TRUE";

    @Override
    public void save(TeacherCourse teacherCourse) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_ASSIGNMENT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, teacherCourse.getTeacherId());
            stmt.setInt(2, teacherCourse.getCourseId());
            stmt.setBoolean(3, teacherCourse.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        teacherCourse.setTeacherCourseId(rs.getInt(1));
                    }
                }
                logger.info("Teacher {} assigned to course {} successfully",
                        teacherCourse.getTeacherId(), teacherCourse.getCourseId());
            }
        } catch (SQLException e) {
            logger.error("Error assigning teacher to course", e);
            throw new RuntimeException("Failed to assign teacher to course", e);
        }
    }

    @Override
    public Optional<TeacherCourse> findById(int teacherCourseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, teacherCourseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacherCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teacher-course assignment by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<TeacherCourse> findCoursesByTeacher(int teacherId) {
        List<TeacherCourse> assignments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TEACHER)) {

            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToTeacherCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding courses by teacher", e);
        }
        return assignments;
    }

    @Override
    public List<TeacherCourse> findTeachersByCourse(int courseId) {
        List<TeacherCourse> assignments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_COURSE)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToTeacherCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teachers by course", e);
        }
        return assignments;
    }

    @Override
    public List<TeacherCourse> findAllActive() {
        List<TeacherCourse> assignments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                assignments.add(mapResultSetToTeacherCourse(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all active assignments", e);
        }
        return assignments;
    }

    @Override
    public void delete(int teacherCourseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_ASSIGNMENT)) {

            stmt.setInt(1, teacherCourseId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Teacher-course assignment deleted successfully with ID: {}", teacherCourseId);
            }
        } catch (SQLException e) {
            logger.error("Error deleting assignment", e);
            throw new RuntimeException("Failed to delete assignment", e);
        }
    }

    @Override
    public void removeTeacherFromCourse(int teacherId, int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(REMOVE_TEACHER_FROM_COURSE)) {

            stmt.setInt(1, teacherId);
            stmt.setInt(2, courseId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Teacher {} removed from course {} successfully", teacherId, courseId);
            }
        } catch (SQLException e) {
            logger.error("Error removing teacher from course", e);
            throw new RuntimeException("Failed to remove teacher from course", e);
        }
    }

    @Override
    public boolean isTeacherAssignedToCourse(int teacherId, int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(CHECK_ASSIGNMENT)) {

            stmt.setInt(1, teacherId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking teacher-course assignment", e);
        }
        return false;
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong("count");
            }
        } catch (SQLException e) {
            logger.error("Error counting active assignments", e);
        }
        return 0;
    }

    private TeacherCourse mapResultSetToTeacherCourse(ResultSet rs) throws SQLException {
        TeacherCourse teacherCourse = new TeacherCourse();
        teacherCourse.setTeacherCourseId(rs.getInt("teacher_course_id"));
        teacherCourse.setTeacherId(rs.getInt("teacher_id"));
        teacherCourse.setCourseId(rs.getInt("course_id"));

        Timestamp assignedDate = rs.getTimestamp("assigned_date");
        if (assignedDate != null) {
            teacherCourse.setAssignedDate(assignedDate.toLocalDateTime());
        }

        Timestamp removedDate = rs.getTimestamp("removed_date");
        if (removedDate != null) {
            teacherCourse.setRemovedDate(removedDate.toLocalDateTime());
        }

        teacherCourse.setActive(rs.getBoolean("is_active"));
        return teacherCourse;
    }
}
