package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of CourseRepository for MySQL database.
 * Handles all course CRUD operations.
 */
public class CourseRepositoryImpl implements CourseRepository {
    private static final Logger logger = LoggerFactory.getLogger(CourseRepositoryImpl.class);

    private static final String INSERT_COURSE = "INSERT INTO courses (course_code, course_name, description, department, credits, semester, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM courses WHERE course_id = ?";

    private static final String SELECT_BY_CODE = "SELECT * FROM courses WHERE course_code = ?";

    private static final String SELECT_ALL = "SELECT * FROM courses ORDER BY course_code";

    private static final String SELECT_ALL_ACTIVE = "SELECT * FROM courses WHERE is_active = TRUE ORDER BY course_code";

    private static final String SELECT_BY_DEPARTMENT = "SELECT * FROM courses WHERE department = ? ORDER BY course_code";

    private static final String SELECT_BY_SEMESTER = "SELECT * FROM courses WHERE semester = ? ORDER BY course_code";

    private static final String UPDATE_COURSE = "UPDATE courses SET course_code = ?, course_name = ?, description = ?, department = ?, credits = ?, semester = ?, is_active = ?, updated_at = NOW() WHERE course_id = ?";

    private static final String DELETE_COURSE = "DELETE FROM courses WHERE course_id = ?";

    private static final String COUNT_ALL = "SELECT COUNT(*) as count FROM courses";

    @Override
    public void save(Course course) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getDescription());
            stmt.setString(4, course.getDepartment());
            stmt.setInt(5, course.getCredits());
            stmt.setInt(6, course.getSemester());
            stmt.setBoolean(7, course.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        course.setCourseId(rs.getInt(1));
                    }
                }
                logger.info("Course saved successfully: {}", course.getCourseCode());
            }
        } catch (SQLException e) {
            logger.error("Error saving course", e);
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Course code already exists: " + course.getCourseCode(), e);
            }
            throw new RuntimeException("Failed to save course: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Course> findById(int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding course by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE)) {

            stmt.setString(1, courseCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding course by code", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all courses", e);
        }
        return courses;
    }

    @Override
    public List<Course> findAllActive() {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching active courses", e);
        }
        return courses;
    }

    @Override
    public List<Course> findByDepartment(String department) {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DEPARTMENT)) {

            stmt.setString(1, department);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching courses by department", e);
        }
        return courses;
    }

    @Override
    public List<Course> findBySemester(int semester) {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_SEMESTER)) {

            stmt.setInt(1, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching courses by semester", e);
        }
        return courses;
    }

    @Override
    public void update(Course course) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_COURSE)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getDescription());
            stmt.setString(4, course.getDepartment());
            stmt.setInt(5, course.getCredits());
            stmt.setInt(6, course.getSemester());
            stmt.setBoolean(7, course.isActive());
            stmt.setInt(8, course.getCourseId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Course updated successfully: {}", course.getCourseCode());
            }
        } catch (SQLException e) {
            logger.error("Error updating course", e);
            throw new RuntimeException("Failed to update course", e);
        }
    }

    @Override
    public void delete(int courseId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_COURSE)) {

            stmt.setInt(1, courseId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Course deleted successfully with ID: {}", courseId);
            }
        } catch (SQLException e) {
            logger.error("Error deleting course", e);
            throw new RuntimeException("Failed to delete course", e);
        }
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
            logger.error("Error counting courses", e);
        }
        return 0;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setDescription(rs.getString("description"));
        course.setDepartment(rs.getString("department"));
        course.setCredits(rs.getInt("credits"));
        course.setSemester(rs.getInt("semester"));
        course.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            course.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            course.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return course;
    }
}
