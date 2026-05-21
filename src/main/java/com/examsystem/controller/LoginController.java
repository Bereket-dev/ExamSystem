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
        logger.info("Login attempt started");

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
            logger.warn("Login attempt with empty credentials");
            return;
        }

        try {
            // Try to find user by username
            var userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                showError("Invalid username or password");
                logger.warn("Login failed: user {} not found", username);
                return;
            }

            User user = userOptional.get();

            // Simple password verification (in production, use hashing)
            if (!user.getPassword().equals(password)) {
                showError("Invalid username or password");
                logger.warn("Login failed: incorrect password for user {}", username);
                return;
            }

            // Check if user is active
            if (!user.isActive()) {
                showError("User account is inactive");
                logger.warn("Login failed: user {} is inactive", username);
                return;
            }

            // Login successful
            Session.getInstance().login(user);
            logger.info("User {} successfully logged in with role {}", username, user.getRole());

            // Navigate based on role
            navigateByRole(user.getRole());

        } catch (Exception e) {
            logger.error("Error during login", e);
            showError("An error occurred during login. Please try again.");
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText("Welcome!");
            alert.setContentText(String.format("Welcome %s!\n\nLogged in as: %s\nRole: %s\n\n%s",
                    user.getFullName(),
                    user.getUsername(),
                    user.getRole(),
                    dashboardName + " loading..."));
            alert.showAndWait();

            logger.info("Showing dashboard: {}", dashboardName);
            // TODO: Load actual dashboard FXML
        } catch (Exception e) {
            logger.error("Error showing dashboard", e);
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Display error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
        } else {
            logger.error("Error: {}", message);
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
