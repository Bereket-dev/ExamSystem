package com.examsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.examsystem.model.User;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.FormValidator;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.application.Platform;

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
            errorLabel.getStyleClass().add("status-error");
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
        errorLabel.setText("");
        FormValidator.clearErrors(usernameField, passwordField);

        FormValidator.ValidationResult validation = FormValidator.combine(
                FormValidator.required(usernameField, "Username"),
                FormValidator.required(passwordField, "Password"));
        if (!validation.isValid()) {
            FormValidator.applyResult(validation, errorLabel);
            UiManager.shake(usernameField);
            passwordField.clear();
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        loginButton.setDisable(true);
        logger.info("Login attempt started on background thread");

        BackgroundLoader.load(
                () -> authenticateUser(username, password),
                user -> Platform.runLater(() -> completeLogin(user, username, password)),
                error -> Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showError("Error: " + error.getMessage());
                    passwordField.clear();
                }));
    }

    private User authenticateUser(String username, String password) {
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Incorrect username or password");
        }
        User user = userOptional.get();
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect username or password");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }
        return user;
    }

    private void completeLogin(User user, String username, String password) {
        try {
            Session.getInstance().login(user);
            connectToNetwork(user, username, password);
            navigateByRole(user.getRole());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            passwordField.clear();
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void connectToNetwork(User user, String username, String password) {
        NetworkManager network = NetworkManager.getInstance();
        RMIManager rmi = RMIManager.getInstance();
        try {
            if (user.getRole() == User.UserRole.TEACHER || user.getRole() == User.UserRole.ADMIN) {
                network.startServer();
                rmi.startServer();
                logger.info("TCP and RMI servers started for {}", user.getRole());
            }
            if (user.getRole() == User.UserRole.STUDENT) {
                boolean tcpOk = network.connectClient();
                if (tcpOk) {
                    network.clientLogin(username, password);
                    logger.info("Student connected to TCP server");
                }
                if (rmi.connectClient()) {
                    rmi.clientLogin(username, password);
                    logger.info("Student connected to RMI registry");
                }
                if (!tcpOk && !rmi.isClientConnected()) {
                    logger.warn("Student running in offline mode (servers unavailable)");
                }
            }
        } catch (Exception e) {
            logger.warn("Network connection skipped: {}", e.getMessage());
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
                logger.info("Navigating to Admin Panel");
                navigateToAdminPanel();
                break;
            case TEACHER:
                logger.info("Navigating to Teacher Dashboard");
                navigateToTeacherDashboard();
                break;
            case STUDENT:
                logger.info("Navigating to Student Dashboard");
                navigateToStudentDashboard();
                break;
            default:
                logger.warn("Unknown role: {}", role);
        }
    }

    private void navigateToTeacherDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
            Parent root = loader.load();
            TeacherDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            logger.error("Error navigating to teacher dashboard", e);
            showError("Unable to open teacher dashboard: " + e.getMessage());
        }
    }

    private void navigateToStudentDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            StudentDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Student Dashboard - ExamSystem");
        } catch (Exception e) {
            logger.error("Error navigating to student dashboard", e);
            showError("Unable to open student dashboard: " + e.getMessage());
        }
    }

    private void navigateToAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/AdminPanel.fxml"));
            Parent root = loader.load();
            AdminPanelController controller = loader.getController();
            controller.setCurrentAdmin(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Admin Panel - ExamSystem");
        } catch (Exception e) {
            logger.error("Error navigating to admin panel", e);
            showError("Unable to open admin panel: " + e.getMessage());
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
            errorLabel.getStyleClass().removeAll("status-success");
            errorLabel.getStyleClass().add("status-error");
            errorLabel.setText(message);
            errorLabel.setWrapText(true);
        }
        if (usernameField != null) {
            UiManager.shake(usernameField);
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
