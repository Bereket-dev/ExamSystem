package com.examsystem.util;

import com.examsystem.controller.SyncChromeController;
import com.examsystem.sync.SyncManager;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Wraps dashboard content with the global sync status bar.
 */
public final class NavigationHelper {
    private NavigationHelper() {
    }

    public static void openAppScreen(Stage stage, Parent content, String title) throws Exception {
        VBox shell = new VBox();
        shell.getChildren().add(SyncChromeController.load());
        shell.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);

        SyncManager.getInstance().setPrimaryStage(stage);
        UiManager.navigateToApp(stage, shell, title);
    }
}
