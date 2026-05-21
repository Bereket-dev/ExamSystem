package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TeacherRepositoryImpl implements TeacherRepository {
    private static final Logger logger = LoggerFactory.getLogger(TeacherRepositoryImpl.class);
    private static final String SELECT_BY_USER = "SELECT * FROM teachers WHERE user_id = ?";

    @Override
    public Optional<Teacher> findByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(rs.getInt("teacher_id"));
                    teacher.setUserId(rs.getInt("user_id"));
                    teacher.setDepartment(rs.getString("department"));
                    teacher.setQualification(rs.getString("qualification"));
                    teacher.setExperienceYears(rs.getInt("experience_years"));
                    return Optional.of(teacher);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teacher by user id", e);
        }
        return Optional.empty();
    }
}
