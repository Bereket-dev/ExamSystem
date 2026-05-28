package com.examsystem.util;

import com.examsystem.sync.SyncManager;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Opens an application screen with sync chrome (via {@link UiManager} / {@link SyncUiHelper}).
 */
public final class NavigationHelper {
    private NavigationHelper() {
    }

    public static void openAppScreen(Stage stage, Parent content, String title) {
        SyncManager.getInstance().setPrimaryStage(stage);
        UiManager.navigateToApp(stage, content, title);
    }
}
