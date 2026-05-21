package com.examsystem.util;

import com.examsystem.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session management for authenticated users.
 * Manages the current logged-in user and session state.
 * Phase 3 - Authentication System
 */
public class Session {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    private static Session instance;
    private User currentUser;
    private boolean isAuthenticated;
    private long loginTime;

    private Session() {
        this.isAuthenticated = false;
        this.currentUser = null;
        this.loginTime = 0;
    }

    /**
     * Get singleton instance of Session
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
            logger.info("Session instance created");
        }
        return instance;
    }

    /**
     * Login a user
     */
    public void login(User user) {
        this.currentUser = user;
        this.isAuthenticated = true;
        this.loginTime = System.currentTimeMillis();
        logger.info("User {} logged in with role {}", user.getUsername(), user.getRole());
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User {} logged out", currentUser.getUsername());
        }
        this.currentUser = null;
        this.isAuthenticated = false;
        this.loginTime = 0;
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Get user role
     */
    public String getUserRole() {
        return isAuthenticated && currentUser != null ? currentUser.getRole().toString() : null;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return isAuthenticated && currentUser != null &&
                currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Check if user is teacher
     */
    public boolean isTeacher() {
        return isAuthenticated && currentUser != null &&
                currentUser.getRole() == User.UserRole.TEACHER;
    }

    /**
     * Check if user is student
     */
    public boolean isStudent() {
        return isAuthenticated && currentUser != null &&
                currentUser.getRole() == User.UserRole.STUDENT;
    }

    /**
     * Get session duration in seconds
     */
    public long getSessionDuration() {
        if (!isAuthenticated)
            return 0;
        return (System.currentTimeMillis() - loginTime) / 1000;
    }

    /**
     * Clear session (used for testing)
     */
    public void clear() {
        logout();
    }
}
