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
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.FormValidator;
import com.examsystem.util.Session;
import com.examsystem.util.NavigationHelper;
import com.examsystem.util.UiManager;
import com.examsystem.sync.ui.SyncProgressDialog;
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
            // Always show connection setup after login; saved values are pre-filled on that screen.
            navigateToConnectionSetup(user);
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            passwordField.clear();
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void navigateToConnectionSetup(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/examsystem/fxml/ConnectionSetup.fxml"));
            Parent root = loader.load();
            ConnectionSetupController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            UiManager.navigate(stage, root, UiManager.APP_WIDTH, UiManager.APP_HEIGHT,
                    "Server Connection Setup - ExamSystem");
        } catch (Exception e) {
            logger.error("Failed to open connection setup", e);
            showError("Could not open connection setup: " + e.getMessage());
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
        openRoleDashboard(
                "/com/examsystem/fxml/TeacherDashboard.fxml",
                "Teacher Dashboard - ExamSystem",
                "teacher dashboard",
                root -> ((TeacherDashboardController) root).setUser(Session.getInstance().getCurrentUser()));
    }

    private void navigateToStudentDashboard() {
        openRoleDashboard(
                "/com/examsystem/fxml/StudentDashboard.fxml",
                "Student Dashboard - ExamSystem",
                "student dashboard",
                root -> ((StudentDashboardController) root).setUser(Session.getInstance().getCurrentUser()));
    }

    private void navigateToAdminPanel() {
        openRoleDashboard(
                "/com/examsystem/fxml/AdminPanel.fxml",
                "Admin Panel - ExamSystem",
                "admin panel",
                root -> ((AdminPanelController) root).setCurrentAdmin(Session.getInstance().getCurrentUser()));
    }

    private void openRoleDashboard(String fxmlPath, String title, String screenName,
            java.util.function.Consumer<Object> controllerSetup) {
        try {
            var fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showError("Screen resource not found: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            controllerSetup.accept(loader.getController());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            NavigationHelper.openAppScreen(stage, root, title);
            try {
                SyncProgressDialog.show(stage, null);
            } catch (Exception syncUi) {
                logger.debug("Sync progress dialog skipped: {}", syncUi.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error navigating to {}", screenName, e);
            showError("Unable to open " + screenName + ". " + friendlyNavigationError(e));
        }
    }

    private static String friendlyNavigationError(Throwable e) {
        if (e == null) {
            return "Please try again.";
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        if (msg.contains("Location is required") || msg.contains("resource not found")) {
            return "A required screen file is missing from the build. Run: mvnd clean compile";
        }
        return msg;
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
