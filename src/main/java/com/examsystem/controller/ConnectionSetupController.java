package com.examsystem.controller;

import com.examsystem.connection.ConnectionProfile;
import com.examsystem.connection.ConnectionSettingsStore;
import com.examsystem.connection.ConnectionSetupService;
import com.examsystem.connection.ConnectionTestResult;
import com.examsystem.model.User;
import com.examsystem.sync.SyncManager;
import com.examsystem.sync.ui.SyncProgressDialog;
import com.examsystem.util.AuthLogoutHelper;
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.ConfigManager;
import com.examsystem.util.NavigationHelper;
import com.examsystem.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-login connectivity wizard — admin server host vs teacher/student client setup.
 */
public class ConnectionSetupController {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionSetupController.class);

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label hintLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox adminServerPanel;

    @FXML
    private Region adminStatusDot;

    @FXML
    private Label adminPortLabel;

    @FXML
    private Button adminContinueButton;

    @FXML
    private Button adminOfflineButton;

    @FXML
    private VBox clientConnectionPanel;

    @FXML
    private TextField serverIpField;

    @FXML
    private TextField rmiPortField;

    @FXML
    private Button testConnectionButton;

    @FXML
    private Button saveContinueButton;

    @FXML
    private Button clientOfflineButton;

    @FXML
    private Region statusDot;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private Label messageLabel;

    private User currentUser;
    private boolean adminMode;
    private volatile boolean lastTestSuccessful;

    @FXML
    public void initialize() {
        logoutButton.setOnAction(e -> handleLogout());
    }

    public void setUser(User user) {
        this.currentUser = user;
        this.adminMode = user.getRole() == User.UserRole.ADMIN;

        welcomeLabel.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");

        int defaultPort = ConfigManager.getIntProperty("rmi.registry.port", 1099);
        ConnectionProfile saved = ConnectionSettingsStore.load();

        if (adminMode) {
            configureAdminPanel(saved, defaultPort);
        } else {
            configureClientPanel(saved, defaultPort);
        }

        if (saved.isSetupCompleted()) {
            String mode = saved.isOfflineMode() ? "offline (H2)" : "online (RMI)";
            setMessage("Previous settings loaded (" + mode + "). Choose how to continue below.",
                    "connection-message");
        }
    }

    private void configureAdminPanel(ConnectionProfile saved, int defaultPort) {
        pageTitleLabel.setText("Central Server");
        hintLabel.setText(
                "You are the system administrator. Start the central server, continue offline, or log out.");

        adminServerPanel.setVisible(true);
        adminServerPanel.setManaged(true);
        clientConnectionPanel.setVisible(false);
        clientConnectionPanel.setManaged(false);

        int port = saved.getRmiPort() > 0 ? saved.getRmiPort() : defaultPort;
        adminPortLabel.setText("RMI registry port: " + port);
        if (!saved.isSetupCompleted()) {
            setMessage("Server Mode ON — ready to host teacher and student connections.", "connection-message-success");
        }

        adminContinueButton.setOnAction(e -> handleAdminContinue(port));
        adminOfflineButton.setOnAction(e -> handleContinueOffline());
    }

    private void configureClientPanel(ConnectionProfile saved, int defaultPort) {
        pageTitleLabel.setText("Server Connection Setup");
        hintLabel.setText(
                "Enter the admin computer's IP address, test the connection, save to continue online, or use Continue Offline.");

        adminServerPanel.setVisible(false);
        adminServerPanel.setManaged(false);
        clientConnectionPanel.setVisible(true);
        clientConnectionPanel.setManaged(true);

        serverIpField.setText(saved.getServerHost());
        rmiPortField.setText(String.valueOf(saved.getRmiPort() > 0 ? saved.getRmiPort() : defaultPort));
        setStatusIdle("Click Test Connection to verify the admin server.");

        testConnectionButton.setOnAction(e -> handleTestConnection());
        saveContinueButton.setOnAction(e -> handleSaveAndContinue());
        clientOfflineButton.setOnAction(e -> handleContinueOffline());
    }

    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            AuthLogoutHelper.logoutToLogin(stage);
        } catch (Exception e) {
            logger.error("Logout from connection setup failed", e);
            setMessage("Logout failed. Please close the application and try again.", "connection-message-error");
        }
    }

    private void handleAdminContinue(int port) {
        setActionButtonsDisabled(true);
        setMessage("Starting central server…", "connection-message");

        ConnectionProfile profile = ConnectionSetupService.adminServerProfile();
        profile.setRmiPort(port);

        BackgroundLoader.load(
                () -> {
                    try {
                        ConnectionSetupService.applyAdminServerMode(currentUser, profile);
                        return true;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ok -> Platform.runLater(() -> {
                    setActionButtonsDisabled(false);
                    setMessage("Central server started — syncing local backup…", "connection-message-success");
                    pullThenOpenDashboard();
                }),
                error -> Platform.runLater(() -> {
                    setActionButtonsDisabled(false);
                    setMessage(
                            "Server start had an issue. You can retry, continue offline, or log out.",
                            "connection-message-warning");
                    logger.warn("Admin server start: {}", error.getMessage());
                }));
    }

    private void handleTestConnection() {
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

        setClientButtonsDisabled(true);
        setStatusPending("Testing connection…");

        BackgroundLoader.load(
                () -> ConnectionSetupService.testRmiConnection(host, port),
                result -> Platform.runLater(() -> {
                    setClientButtonsDisabled(false);
                    lastTestSuccessful = result.success();
                    if (result.success()) {
                        setStatusSuccess("Connected successfully");
                    } else {
                        setStatusError("Connection failed — try again or use Continue Offline.");
                    }
                }),
                error -> Platform.runLater(() -> {
                    setClientButtonsDisabled(false);
                    lastTestSuccessful = false;
                    setStatusError("Connection failed — try again or use Continue Offline.");
                }));
    }

    private void handleSaveAndContinue() {
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
        if (!lastTestSuccessful) {
            setMessage(
                    "Run Test Connection first, or choose Continue Offline if the server is unavailable.",
                    "connection-message-warning");
            return;
        }

        setClientButtonsDisabled(true);
        setMessage("Saving and connecting…", "connection-message");

        ConnectionProfile profile = ConnectionSetupService.profileFromInputs(host, port);
        BackgroundLoader.load(
                () -> {
                    try {
                        ConnectionSetupService.applyClientOnlineMode(currentUser, profile);
                        return true;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ok -> Platform.runLater(() -> {
                    setClientButtonsDisabled(false);
                    setStatusSuccess("Connected — pulling data from server…");
                    pullThenOpenDashboard();
                }),
                error -> Platform.runLater(() -> {
                    setClientButtonsDisabled(false);
                    setMessage(
                            "Could not connect online. Retry, use Continue Offline, or Logout.",
                            "connection-message-warning");
                }));
    }

    private void handleContinueOffline() {
        setActionButtonsDisabled(true);
        setMessage("Activating offline mode…", "connection-message");

        ConnectionProfile profile = adminMode
                ? ConnectionSetupService.adminServerProfile()
                : ConnectionSetupService.profileFromInputs(
                        serverIpField != null && !serverIpField.getText().trim().isEmpty()
                                ? serverIpField.getText().trim()
                                : "localhost",
                        parsePort() > 0 ? parsePort() : ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(true);

        BackgroundLoader.load(
                () -> {
                    ConnectionSetupService.applyOfflineModeResilient(currentUser, profile);
                    return true;
                },
                ok -> Platform.runLater(() -> {
                    setActionButtonsDisabled(false);
                    setMessage("Offline Mode Activated — using local H2 backup.", "connection-message-warning");
                    openDashboardSafely();
                }),
                error -> Platform.runLater(() -> {
                    setActionButtonsDisabled(false);
                    openDashboardSafely();
                }));
    }

    /** Pull central data over RMI, then open dashboard when successful. */
    private void pullThenOpenDashboard() {
        try {
            Stage stage = resolveStage();
            SyncManager sync = SyncManager.getInstance();
            sync.setPrimaryStage(stage);
            setMessage("Pulling data from central server…", "connection-message");
            SyncProgressDialog.show(stage, success -> Platform.runLater(() -> {
                if (success) {
                    openDashboardSafely();
                } else {
                    setMessage(
                            "Could not download data from the server. Check the admin is online, then retry or use Continue Offline.",
                            "connection-message-warning");
                    setActionButtonsDisabled(false);
                }
            }));
        } catch (Exception e) {
            logger.error("Server pull failed to start", e);
            setMessage("Could not start sync: " + e.getMessage(), "connection-message-error");
            setActionButtonsDisabled(false);
        }
    }

    /** Never blocks the user on navigation failures. */
    private void openDashboardSafely() {
        try {
            SyncManager.getInstance().startBackgroundSync();
            User user = currentUser != null ? currentUser : Session.getInstance().getCurrentUser();
            if (user == null) {
                handleLogout();
                return;
            }
            Stage stage = resolveStage();
            switch (user.getRole()) {
                case ADMIN -> openAdminPanel(stage, false);
                case TEACHER -> openTeacherDashboard(stage, false);
                case STUDENT -> openStudentDashboard(stage, false);
                default -> setMessage("Unknown role. Use Logout to sign in again.", "connection-message-error");
            }
        } catch (Exception e) {
            logger.error("Dashboard navigation failed", e);
            setMessage(
                    "Dashboard could not load fully. Try Continue Offline again or Logout.",
                    "connection-message-warning");
            setActionButtonsDisabled(false);
        }
    }

    private Stage resolveStage() {
        if (logoutButton != null && logoutButton.getScene() != null) {
            return (Stage) logoutButton.getScene().getWindow();
        }
        if (adminContinueButton != null && adminContinueButton.getScene() != null) {
            return (Stage) adminContinueButton.getScene().getWindow();
        }
        if (saveContinueButton != null && saveContinueButton.getScene() != null) {
            return (Stage) saveContinueButton.getScene().getWindow();
        }
        throw new IllegalStateException("No stage available");
    }

    private void openAdminPanel(Stage stage, boolean showSyncDialog) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/AdminPanel.fxml"));
        Parent root = loader.load();
        AdminPanelController adminController = loader.getController();
        adminController.setCurrentAdmin(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Admin Panel - ExamSystem");
        if (showSyncDialog) {
            tryShowSyncProgress(stage);
        }
    }

    private void openTeacherDashboard(Stage stage, boolean showSyncDialog) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
        Parent root = loader.load();
        TeacherDashboardController teacherController = loader.getController();
        teacherController.setUser(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Teacher Dashboard - ExamSystem");
        if (showSyncDialog) {
            tryShowSyncProgress(stage);
        }
    }

    private void openStudentDashboard(Stage stage, boolean showSyncDialog) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDashboard.fxml"));
        Parent root = loader.load();
        StudentDashboardController studentController = loader.getController();
        studentController.setUser(Session.getInstance().getCurrentUser());
        NavigationHelper.openAppScreen(stage, root, "Student Dashboard - ExamSystem");
        if (showSyncDialog) {
            tryShowSyncProgress(stage);
        }
    }

    private void tryShowSyncProgress(Stage stage) {
        try {
            SyncManager.getInstance().setPrimaryStage(stage);
            SyncProgressDialog.show(stage, null);
        } catch (Exception e) {
            logger.debug("Sync progress dialog skipped: {}", e.getMessage());
        }
    }

    private int parsePort() {
        if (rmiPortField == null) {
            return ConfigManager.getIntProperty("rmi.registry.port", 1099);
        }
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

    private void setActionButtonsDisabled(boolean disabled) {
        if (logoutButton != null) {
            logoutButton.setDisable(false);
        }
        if (adminMode) {
            if (adminContinueButton != null) {
                adminContinueButton.setDisable(disabled);
            }
            if (adminOfflineButton != null) {
                adminOfflineButton.setDisable(disabled);
            }
        } else {
            setClientButtonsDisabled(disabled);
        }
    }

    private void setClientButtonsDisabled(boolean disabled) {
        if (testConnectionButton != null) {
            testConnectionButton.setDisable(disabled);
        }
        if (saveContinueButton != null) {
            saveContinueButton.setDisable(disabled);
        }
        if (clientOfflineButton != null) {
            clientOfflineButton.setDisable(disabled);
        }
    }

    private void setStatusIdle(String message) {
        lastTestSuccessful = false;
        applyClientStatusDot("connection-dot-idle", message);
    }

    private void setStatusPending(String message) {
        applyClientStatusDot("connection-dot-pending", message);
    }

    private void setStatusSuccess(String message) {
        applyClientStatusDot("connection-dot-success", message);
        setMessage(message, "connection-message-success");
    }

    private void setStatusError(String message) {
        applyClientStatusDot("connection-dot-error", message);
        setMessage(message, "connection-message-error");
    }

    private void applyClientStatusDot(String dotClass, String statusText) {
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
    }

    private void setMessage(String text, String styleClass) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText(text);
        messageLabel.getStyleClass().removeAll(
                "connection-message", "connection-message-success",
                "connection-message-error", "connection-message-warning");
        messageLabel.getStyleClass().add(styleClass);
    }
}
