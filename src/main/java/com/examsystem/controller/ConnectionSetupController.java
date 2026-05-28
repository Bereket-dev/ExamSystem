package com.examsystem.controller;

import com.examsystem.connection.ConnectionProfile;
import com.examsystem.connection.ConnectionSettingsStore;
import com.examsystem.connection.ConnectionSetupService;
import com.examsystem.connection.ConnectionTestResult;
import com.examsystem.model.User;
import com.examsystem.rmi.RMIManager;
import com.examsystem.sync.ui.SyncProgressDialog;
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.ConfigManager;
import com.examsystem.util.NavigationHelper;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-login wizard for RMI server configuration and offline mode.
 */
public class ConnectionSetupController {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionSetupController.class);

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField serverIpField;

    @FXML
    private TextField rmiPortField;

    @FXML
    private Button testConnectionButton;

    @FXML
    private Button saveContinueButton;

    @FXML
    private Button offlineButton;

    @FXML
    private Region statusDot;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private Label messageLabel;

    private User currentUser;
    private volatile boolean lastTestSuccessful;

    @FXML
    public void initialize() {
        ConnectionProfile saved = ConnectionSettingsStore.load();
        serverIpField.setText(saved.getServerHost());
        rmiPortField.setText(String.valueOf(saved.getRmiPort()));
        setStatusIdle("Click Test Connection to verify the admin server.");

        testConnectionButton.setOnAction(e -> handleTestConnection());
        saveContinueButton.setOnAction(e -> handleSaveAndContinue());
        offlineButton.setOnAction(e -> handleContinueOffline());
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
    }

    private void handleTestConnection() {
        messageLabel.setText("");

        String host = serverIpField.getText().trim();
        int port = parsePort();
        if (host.isEmpty()) {
            setStatusError("Please enter the admin server IP address.");
            return;
        }
        if (port < 1) {
            setStatusError("Enter a valid RMI port (default 1099).");
            return;
        }

        setStatusPending("Testing connection…");
        setButtonsDisabled(true);

        BackgroundLoader.load(
                () -> {
                    if (currentUser != null && currentUser.getRole() == User.UserRole.ADMIN) {
                        RMIManager.getInstance().startServer();
                    }
                    return ConnectionSetupService.testRmiConnection(host, port);
                },
                result -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    lastTestSuccessful = result.success();
                    if (result.success()) {
                        setStatusSuccess(result.message());
                    } else {
                        setStatusError(result.message());
                    }
                }),
                error -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    lastTestSuccessful = false;
                    setStatusError(error.getMessage());
                }));
    }

    private void handleSaveAndContinue() {
        messageLabel.setText("");
        String host = serverIpField.getText().trim();
        int port = parsePort();
        if (host.isEmpty()) {
            setStatusError("Please enter the admin server IP address.");
            return;
        }
        if (port < 1 || port > 65535) {
            setStatusError("RMI port must be between 1 and 65535.");
            return;
        }

        boolean requireTest = currentUser != null && currentUser.getRole() != User.UserRole.ADMIN;
        if (requireTest && !lastTestSuccessful) {
            setStatusError("Please run Test Connection successfully before saving online settings.");
            return;
        }

        setButtonsDisabled(true);
        setStatusPending("Connecting and saving configuration…");

        ConnectionProfile profile = ConnectionSetupService.profileFromInputs(host, port);
        BackgroundLoader.load(
                () -> {
                    try {
                        ConnectionSetupService.applyOnlineMode(currentUser, profile);
                        return true;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ok -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    navigateToDashboard();
                }),
                error -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    setStatusError(error.getMessage());
                }));
    }

    private void handleContinueOffline() {
        messageLabel.setText("");
        setButtonsDisabled(true);
        setStatusPending("Starting offline mode with local backup database…");

        ConnectionProfile profile = ConnectionSetupService.profileFromInputs(
                serverIpField.getText().trim().isEmpty() ? "localhost" : serverIpField.getText().trim(),
                parsePort() > 0 ? parsePort() : ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(true);

        BackgroundLoader.load(
                () -> {
                    try {
                        ConnectionSetupService.applyOfflineMode(currentUser, profile);
                        return true;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ok -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    setStatusWarning("Offline Mode Activated — working from local H2 backup.");
                    navigateToDashboard();
                }),
                error -> Platform.runLater(() -> {
                    setButtonsDisabled(false);
                    setStatusError(error.getMessage());
                }));
    }

    private void navigateToDashboard() {
        try {
            User user = currentUser != null ? currentUser : Session.getInstance().getCurrentUser();
            Stage stage = (Stage) saveContinueButton.getScene().getWindow();

            switch (user.getRole()) {
                case ADMIN -> openAdminPanel(stage);
                case TEACHER -> openTeacherDashboard(stage);
                case STUDENT -> openStudentDashboard(stage);
                default -> setStatusError("Unknown user role.");
            }
        } catch (Exception e) {
            logger.error("Navigation after connection setup failed", e);
            setStatusError("Could not open dashboard: " + e.getMessage());
        }
    }

    private void openAdminPanel(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/AdminPanel.fxml"));
        Parent root = loader.load();
        AdminPanelController controller = loader.getController();
        controller.setCurrentAdmin(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Admin Panel - ExamSystem");
        SyncProgressDialog.show(stage, null);
    }

    private void openTeacherDashboard(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
        Parent root = loader.load();
        TeacherDashboardController controller = loader.getController();
        controller.setUser(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Teacher Dashboard - ExamSystem");
        SyncProgressDialog.show(stage, null);
    }

    private void openStudentDashboard(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDashboard.fxml"));
        Parent root = loader.load();
        StudentDashboardController controller = loader.getController();
        controller.setUser(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Student Dashboard - ExamSystem");
        SyncProgressDialog.show(stage, null);
    }

    private int parsePort() {
        String text = rmiPortField.getText().trim();
        if (text.isEmpty()) {
            return ConfigManager.getIntProperty("rmi.registry.port", 1099);
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void setButtonsDisabled(boolean disabled) {
        testConnectionButton.setDisable(disabled);
        saveContinueButton.setDisable(disabled);
        offlineButton.setDisable(disabled);
    }

    private void setStatusIdle(String message) {
        lastTestSuccessful = false;
        applyStatusStyle("connection-dot-idle", message, "connection-message");
    }

    private void setStatusPending(String message) {
        applyStatusStyle("connection-dot-pending", message, "connection-message");
    }

    private void setStatusSuccess(String message) {
        applyStatusStyle("connection-dot-success", message, "connection-message-success");
    }

    private void setStatusError(String message) {
        applyStatusStyle("connection-dot-error", message, "connection-message-error");
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    private void setStatusWarning(String message) {
        applyStatusStyle("connection-dot-warning", message, "connection-message-warning");
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    private void applyStatusStyle(String dotClass, String statusText, String messageStyle) {
        if (statusDot != null) {
            statusDot.getStyleClass().removeIf(c -> c.startsWith("connection-dot-"));
            if (!statusDot.getStyleClass().contains("connection-dot")) {
                statusDot.getStyleClass().add("connection-dot");
            }
            statusDot.getStyleClass().add(dotClass);
        }
        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText(statusText);
        }
        if (messageLabel != null) {
            messageLabel.getStyleClass().removeAll(
                    "connection-message", "connection-message-success",
                    "connection-message-error", "connection-message-warning");
            messageLabel.getStyleClass().add(messageStyle);
        }
    }
}
