package com.examsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.examsystem.model.User;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;
import com.examsystem.util.Session;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation.
 * Phase 3 - Authentication System
 */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private UserRepository userRepository;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label titleLabel;

    public LoginController() {
        this.userRepository = new UserRepositoryImpl();
    }

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        logger.info("Initializing LoginController");
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setStyle("-fx-text-fill: red;");
        }

        if (loginButton != null) {
            loginButton.setOnAction(event -> handleLogin());
        }

        if (signupButton != null) {
            signupButton.setOnAction(event -> handleSignup());
        }
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        try {
            logger.info("Login attempt started");

            // Clear previous error
            errorLabel.setText("");

            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                showError("Username and password are required");
                passwordField.clear();
                logger.warn("Login attempt with empty credentials");
                return;
            }

            logger.info("Attempting to find user: {}", username);

            // Try to find user by username
            var userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                showError("Incorrect username or password");
                passwordField.clear();
                logger.warn("Login failed: user {} not found", username);
                return;
            }

            User user = userOptional.get();
            logger.info("User found: {}, checking password", username);

            // Simple password verification (in production, use hashing)
            if (!user.getPassword().equals(password)) {
                showError("Incorrect username or password");
                passwordField.clear();
                logger.warn("Login failed: incorrect password for user {}", username);
                return;
            }

            logger.info("Password verified for user: {}", username);

            // Check if user is active
            if (!user.isActive()) {
                showError("User account is inactive");
                passwordField.clear();
                logger.warn("Login failed: user {} is inactive", username);
                return;
            }

            // Login successful
            logger.info("Login successful for user: {}, Role: {}", username, user.getRole());
            Session.getInstance().login(user);

            // Navigate based on role
            navigateByRole(user.getRole());

        } catch (Exception e) {
            logger.error("Critical error during login", e);
            e.printStackTrace();
            showError("Error: " + e.getMessage());
            passwordField.clear();
        }
    }

    /**
     * Handle signup button click
     */
    @FXML
    private void handleSignup() {
        logger.info("Signup requested");
        showError("Sign up feature coming soon!");
        // TODO: Implement signup screen
    }

    /**
     * Navigate to appropriate screen based on user role
     */
    private void navigateByRole(User.UserRole role) {
        logger.info("Navigating user to {} dashboard", role);

        switch (role) {
            case ADMIN:
                logger.info("Navigating to Admin Dashboard");
                showDashboard("Admin Dashboard");
                break;
            case TEACHER:
                logger.info("Navigating to Teacher Dashboard");
                showDashboard("Teacher Dashboard");
                break;
            case STUDENT:
                logger.info("Navigating to Student Dashboard");
                showDashboard("Student Dashboard");
                break;
            default:
                logger.warn("Unknown role: {}", role);
        }
    }

    /**
     * Display dashboard (temporary implementation for Phase 3)
     */
    private void showDashboard(String dashboardName) {
        try {
            User user = Session.getInstance().getCurrentUser();

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Login Successful");
            successAlert.setHeaderText("Welcome " + user.getFullName() + "!");
            successAlert.setContentText(String.format(
                    "Username: %s\nRole: %s\n\n%s is loading...",
                    user.getUsername(),
                    user.getRole(),
                    dashboardName));
            successAlert.showAndWait();

            logger.info("User {} logged in successfully to {}", user.getUsername(), dashboardName);

            // Clear login form for next use
            usernameField.clear();
            passwordField.clear();
            errorLabel.setText("");

            // TODO: Load actual dashboard FXML based on role
            // For now, keep login screen visible

        } catch (Exception e) {
            logger.error("Error showing dashboard", e);
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Display error message
     */
    private void showError(String message) {
        logger.error("Login Error: {}", message);
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13; -fx-font-weight: bold;");
            errorLabel.setWrapText(true);
        }
    }

    /**
     * Get username field (for testing)
     */
    public TextField getUsernameField() {
        return usernameField;
    }

    /**
     * Get password field (for testing)
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Get error label (for testing)
     */
    public Label getErrorLabel() {
        return errorLabel;
    }
}
