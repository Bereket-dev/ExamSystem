package com.examsystem.controller;

import com.examsystem.sync.ConnectionState;
import com.examsystem.sync.SyncManager;
import com.examsystem.sync.ui.SyncProgressDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SyncChromeController {

    @FXML
    private VBox offlineBanner;

    @FXML
    private Label offlineBannerLabel;

    @FXML
    private Label connectionIndicator;

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
                "You are currently working offline. Changes will sync automatically when connection returns.");

        connectionStatusLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> {
                            ConnectionState s = syncManager.connectionStateProperty().get();
                            return s != null ? s.getIndicatorEmoji() + " " + s.getDisplayText() : "";
                        },
                        syncManager.connectionStateProperty()));

        connectionIndicator.textProperty().bind(connectionStatusLabel.textProperty());
        topologyLabel.textProperty().bind(syncManager.topologyTextProperty());
        lastSyncLabel.textProperty().bind(
                javafx.beans.binding.Bindings.concat("Last sync: ", syncManager.lastSyncTimeProperty()));
        pendingLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> "Pending sync: " + syncManager.pendingCountProperty().get() + " changes",
                        syncManager.pendingCountProperty()));
        databaseSourceLabel.textProperty().bind(
                javafx.beans.binding.Bindings.concat("Database: ", syncManager.databaseSourceProperty()));
        syncModeLabel.textProperty().bind(
                javafx.beans.binding.Bindings.concat("Mode: ", syncManager.syncModeProperty()));
        operationLabel.textProperty().bind(syncManager.currentOperationProperty());

        offlineBanner.visibleProperty().bind(syncManager.offlineBannerVisibleProperty());
        offlineBanner.managedProperty().bind(syncManager.offlineBannerVisibleProperty());
        syncSpinner.visibleProperty().bind(syncManager.syncInProgressProperty());
        syncSpinner.managedProperty().bind(syncManager.syncInProgressProperty());

        syncNowButton.disableProperty().bind(syncManager.syncInProgressProperty());
        syncNowButton.setOnAction(e -> {
            Stage stage = syncNowButton.getScene() != null ? (Stage) syncNowButton.getScene().getWindow() : null;
            SyncProgressDialog.show(stage, null);
        });
        syncHistoryButton.setOnAction(e -> openSyncHistory());
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
