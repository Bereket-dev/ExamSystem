package com.examsystem.repository;

import com.examsystem.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Defines CRUD operations for user management.
 */
public interface UserRepository {

    /**
     * Save a new user to the database
     */
    void save(User user);

    /**
     * Find user by ID
     */
    Optional<User> findById(int userId);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Get all users
     */
    List<User> findAll();

    /**
     * Get all users by role
     */
    List<User> findByRole(String role);

    /**
     * Update user information
     */
    void update(User user);

    /**
     * Delete user by ID
     */
    void delete(int userId);

    /**
     * Check if user exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Get total user count
     */
    long count();
}
