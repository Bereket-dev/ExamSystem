package com.examsystem.sync;

import com.examsystem.util.UiManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lightweight auto-dismissing toast notifications.
 */
public class NotificationService {
    private static NotificationService instance;
    private final Map<Stage, StackPane> overlayByStage = new WeakHashMap<>();

    public enum ToastType {
        SUCCESS("toast-success"),
        INFO("toast-info"),
        WARNING("toast-warning"),
        ERROR("toast-error");

        final String styleClass;

        ToastType(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void registerStage(Stage stage) {
        Platform.runLater(() -> {
            if (stage.getScene() == null || !(stage.getScene().getRoot() instanceof StackPane)) {
                return;
            }
            StackPane root = (StackPane) stage.getScene().getRoot();
            overlayByStage.put(stage, root);
        });
    }

    public void show(Stage stage, String message, ToastType type) {
        if (stage == null) {
            return;
        }
        Platform.runLater(() -> displayToast(stage, message, type, 4));
    }

    public void showSuccess(Stage stage, String message) {
        show(stage, message, ToastType.SUCCESS);
    }

    public void showInfo(Stage stage, String message) {
        show(stage, message, ToastType.INFO);
    }

    public void showWarning(Stage stage, String message) {
        show(stage, message, ToastType.WARNING);
    }

    public void showError(Stage stage, String message) {
        show(stage, message, ToastType.ERROR);
    }

    private void displayToast(Stage stage, String message, ToastType type, int seconds) {
        StackPane root = resolveOverlay(stage);
        if (root == null) {
            return;
        }

        Label toast = new Label(message);
        toast.getStyleClass().addAll("sync-toast", type.styleClass);
        toast.setWrapText(true);
        toast.setMaxWidth(360);

        VBox container = new VBox(toast);
        container.setAlignment(Pos.TOP_RIGHT);
        container.setPadding(new Insets(12, 16, 0, 0));
        container.setMouseTransparent(true);
        container.setPickOnBounds(false);

        root.getChildren().add(container);
        StackPane.setAlignment(container, Pos.TOP_RIGHT);

        FadeTransition in = new FadeTransition(Duration.millis(250), container);
        in.setFromValue(0);
        in.setToValue(1);

        FadeTransition out = new FadeTransition(Duration.millis(400), container);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> root.getChildren().remove(container));

        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
        SequentialTransition seq = new SequentialTransition(in, pause, out);
        seq.play();
    }

    private StackPane resolveOverlay(Stage stage) {
        if (overlayByStage.containsKey(stage)) {
            return overlayByStage.get(stage);
        }
        if (stage.getScene() != null && stage.getScene().getRoot() instanceof StackPane sp) {
            overlayByStage.put(stage, sp);
            return sp;
        }
        return null;
    }
}
