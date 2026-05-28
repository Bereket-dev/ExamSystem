package com.examsystem.controller;

import com.examsystem.sync.ConnectionState;
import com.examsystem.sync.SyncManager;
import com.examsystem.sync.ui.SyncProgressDialog;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SyncChromeController {

    @FXML
    private VBox offlineBanner;

    @FXML
    private Label offlineBannerLabel;

    @FXML
    private Region statusDot;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private Label topologyLabel;

    @FXML
    private Label lastSyncLabel;

    @FXML
    private Label pendingLabel;

    @FXML
    private Label databaseSourceLabel;

    @FXML
    private Label syncModeLabel;

    @FXML
    private Label operationLabel;

    @FXML
    private Button syncNowButton;

    @FXML
    private Button syncHistoryButton;

    @FXML
    private ProgressIndicator syncSpinner;

    @FXML
    private HBox syncPanelCard;

    private final SyncManager syncManager = SyncManager.getInstance();

    @FXML
    public void initialize() {
        offlineBannerLabel.setText(
                "Offline Mode Activated — using local H2 backup. Changes will sync when you reconnect online.");

        connectionStatusLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    ConnectionState state = syncManager.connectionStateProperty().get();
                    return state != null ? state.getDisplayText() : ConnectionState.OFFLINE.getDisplayText();
                }, syncManager.connectionStateProperty()));

        syncManager.connectionStateProperty().addListener((obs, oldState, newState) -> updateStatusDot(newState));

        topologyLabel.textProperty().bind(syncManager.topologyTextProperty());
        lastSyncLabel.textProperty().bind(Bindings.concat("Last sync: ", syncManager.lastSyncTimeProperty()));
        pendingLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> "Pending sync: " + syncManager.pendingCountProperty().get() + " changes",
                        syncManager.pendingCountProperty()));
        databaseSourceLabel.textProperty().bind(
                Bindings.concat("Database: ", syncManager.databaseSourceProperty()));
        syncModeLabel.textProperty().bind(Bindings.concat("Mode: ", syncManager.syncModeProperty()));
        operationLabel.textProperty().bind(syncManager.currentOperationProperty());

        offlineBanner.visibleProperty().bind(syncManager.offlineBannerVisibleProperty());
        offlineBanner.managedProperty().bind(syncManager.offlineBannerVisibleProperty());
        syncSpinner.visibleProperty().bind(syncManager.syncInProgressProperty());
        syncSpinner.managedProperty().bind(syncManager.syncInProgressProperty());

        syncNowButton.disableProperty().bind(syncManager.syncInProgressProperty());
        syncNowButton.textProperty().bind(
                Bindings.when(syncManager.syncInProgressProperty())
                        .then("Syncing…")
                        .otherwise("Sync"));

        syncNowButton.setOnAction(e -> triggerManualSync());
        syncHistoryButton.setOnAction(e -> openSyncHistory());

        updateStatusDot(syncManager.connectionStateProperty().get());
        syncManager.refreshConnectionState();
    }

    private void updateStatusDot(ConnectionState state) {
        if (statusDot == null) {
            return;
        }
        statusDot.getStyleClass().removeIf(s -> s.startsWith("sync-dot-") && !"sync-dot".equals(s));
        ConnectionState effective = state != null ? state : ConnectionState.OFFLINE;
        if (!statusDot.getStyleClass().contains("sync-dot")) {
            statusDot.getStyleClass().add("sync-dot");
        }
        statusDot.getStyleClass().add(effective.getDotStyleClass());
    }

    private void triggerManualSync() {
        Stage stage = syncNowButton.getScene() != null ? (Stage) syncNowButton.getScene().getWindow() : null;
        SyncProgressDialog.show(stage, null);
    }

    private void openSyncHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/examsystem/fxml/SyncHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Sync History");
            stage.setScene(new javafx.scene.Scene(root, 720, 480));
            com.examsystem.util.UiManager.applyTheme(root);
            stage.show();
        } catch (Exception ex) {
            operationLabel.setText("Could not open sync history");
        }
    }

    public static Parent load() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SyncChromeController.class.getResource("/com/examsystem/fxml/SyncChrome.fxml"));
        return loader.load();
    }
}
