package com.examsystem.util;

import com.examsystem.controller.SyncChromeController;
import com.examsystem.sync.SyncManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Wraps dashboard roots with sync chrome and toast overlay support.
 */
public final class SyncUiHelper {

    private SyncUiHelper() {
    }

    public static Parent wrapForSync(Parent dashboardRoot, Stage stage) {
        SyncManager.getInstance().setPrimaryStage(stage);

        Parent chrome;
        try {
            chrome = SyncChromeController.load();
        } catch (Exception e) {
            return wrapInStack(dashboardRoot, stage);
        }

        VBox shell = new VBox(chrome, dashboardRoot);
        VBox.setVgrow(dashboardRoot, javafx.scene.layout.Priority.ALWAYS);
        return wrapInStack(shell, stage);
    }

    private static StackPane wrapInStack(Parent content, Stage stage) {
        StackPane stack = new StackPane(content);
        SyncManager.getInstance().setPrimaryStage(stage);
        com.examsystem.sync.NotificationService.getInstance().registerStage(stage);
        return stack;
    }

    public static void attachToStage(Stage stage) {
        if (stage.getScene() == null) {
            return;
        }
        Parent root = stage.getScene().getRoot();
        if (root instanceof StackPane) {
            SyncManager.getInstance().setPrimaryStage(stage);
            NotificationServiceRegister(stage);
            return;
        }
        Parent wrapped = wrapForSync(root, stage);
        stage.getScene().setRoot(wrapped);
    }

    private static void NotificationServiceRegister(Stage stage) {
        com.examsystem.sync.NotificationService.getInstance().registerStage(stage);
    }
}
