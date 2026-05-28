package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of UserRepository for MySQL database.
 * Handles all user CRUD operations.
 */
public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private static final String INSERT_USER = "INSERT INTO users (username, password, email, full_name, role, is_active) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM users WHERE user_id = ?";

    private static final String SELECT_BY_USERNAME = "SELECT * FROM users WHERE username = ?";

    private static final String SELECT_BY_EMAIL = "SELECT * FROM users WHERE email = ?";

    private static final String SELECT_ALL = "SELECT * FROM users ORDER BY user_id";

    private static final String SELECT_BY_ROLE = "SELECT * FROM users WHERE role = ? ORDER BY full_name";

    private static final String UPDATE_USER = "UPDATE users SET password = ?, email = ?, full_name = ?, role = ?, is_active = ?, updated_at = NOW() WHERE user_id = ?";

    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";

    private static final String COUNT_ALL = "SELECT COUNT(*) as count FROM users";

    private static final String EXISTS_USERNAME = "SELECT COUNT(*) as count FROM users WHERE username = ?";

    @Override
    public void save(User user) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole().toString().toLowerCase());
            stmt.setBoolean(6, user.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                logger.info("User saved successfully: {}", user.getUsername());
            }
        } catch (SQLException e) {
            logger.error("Error saving user", e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getAuthConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
        }
        return users;
    }

    @Override
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ROLE)) {

            stmt.setString(1, role.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding users by role", e);
        }
        return users;
    }

    @Override
    public void update(User user) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {

            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole().toString().toLowerCase());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getUserId());

            stmt.executeUpdate();
            logger.info("User updated successfully: {}", user.getUsername());
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(int userId) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_USER)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.info("User deleted successfully with ID: {}", userId);
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(EXISTS_USERNAME)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking user existence", e);
        }
        return false;
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseConnection.getAuthConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(COUNT_ALL)) {

            if (rs.next()) {
                return rs.getLong("count");
            }
        } catch (SQLException e) {
            logger.error("Error counting users", e);
        }
        return 0;
    }

    /**
     * Map ResultSet row to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role").toUpperCase()));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return user;
    }
}
