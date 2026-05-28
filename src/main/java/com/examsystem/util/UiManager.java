package com.examsystem.util;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Phase 9 — Central UI theme, responsive scene sizing, and navigation animations.
 */
public final class UiManager {

    public static final String THEME_CSS = "/css/examsystem.css";

    public static final double LOGIN_WIDTH = 800;
    public static final double LOGIN_HEIGHT = 600;

    public static final double APP_WIDTH = 900;
    public static final double APP_HEIGHT = 650;

    public static final double MIN_WIDTH = 720;
    public static final double MIN_HEIGHT = 520;

    private UiManager() {
    }

    public static void applyTheme(Parent root) {
        String css = UiManager.class.getResource(THEME_CSS).toExternalForm();
        if (!root.getStylesheets().contains(css)) {
            root.getStylesheets().add(css);
        }
    }

    public static Scene createScene(Parent root, double width, double height) {
        applyTheme(root);
        return new Scene(root, width, height);
    }

    public static void configureStage(Stage stage, boolean loginScreen) {
        if (loginScreen) {
            stage.setMinWidth(640);
            stage.setMinHeight(480);
        } else {
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
        }
    }

    /**
     * Replace scene with fade-in animation (Phase 9).
     */
    public static void navigate(Stage stage, Parent root, double width, double height, String title) {
        configureStage(stage, width == LOGIN_WIDTH && height == LOGIN_HEIGHT);
        Scene scene = createScene(root, width, height);
        stage.setTitle(title);
        animateIn(scene.getRoot());
        stage.setScene(scene);
    }

    public static void navigateToApp(Stage stage, Parent root, String title) {
        Parent wrapped = SyncUiHelper.wrapForSync(root, stage);
        navigate(stage, wrapped, APP_WIDTH, APP_HEIGHT, title);
    }

    public static void navigateToLogin(Stage stage, Parent root) {
        navigate(stage, root, LOGIN_WIDTH, LOGIN_HEIGHT, "ExamSystem - Login");
    }

    public static void animateIn(Parent root) {
        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(350), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void shake(Control control) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), control);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
