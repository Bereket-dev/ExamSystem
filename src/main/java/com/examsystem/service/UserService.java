package com.examsystem.service;

import com.examsystem.model.User;
import com.examsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for user-related business operations.
 * Implements business logic for user management.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user
     */
    public void registerUser(String username, String password, String email, String fullName, User.UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(username, password, email, fullName, role);
        userRepository.save(user);
        logger.info("User registered: {}", username);
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(int userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * Update user information
     */
    public void updateUser(User user) {
        userRepository.update(user);
        logger.info("User updated: {}", user.getUsername());
    }

    /**
     * Delete user
     */
    public void deleteUser(int userId) {
        userRepository.delete(userId);
        logger.info("User deleted with ID: {}", userId);
    }

    /**
     * Authenticate user (basic password check)
     */
    public boolean authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            // In production, use proper password hashing (BCrypt, etc.)
            return user.get().getPassword().equals(password) && user.get().isActive();
        }
        return false;
    }

    /**
     * Get total user count
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
}
