package com.examsystem.sync.ui;

import com.examsystem.sync.SyncManager;
import com.examsystem.sync.SyncType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public final class SyncProgressDialog {

    private SyncProgressDialog() {
    }

    public static void show(Window owner, Consumer<Boolean> onComplete) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Synchronizing");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }

        Label stepLabel = new Label("Starting...");
        stepLabel.getStyleClass().add("subsection-text");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.progressProperty().bind(SyncManager.getInstance().syncProgressProperty());

        VBox box = new VBox(12, new Label("Please wait while data is synchronized."), stepLabel, progressBar);
        box.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CANCEL);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CANCEL);
        cancel.setDisable(true);

        SyncManager.getInstance().currentOperationProperty().addListener((obs, o, n) -> Platform.runLater(() -> stepLabel.setText(n)));

        dialog.show();

        SyncManager.getInstance().syncNowWithProgress(success -> Platform.runLater(() -> {
            dialog.close();
            if (onComplete != null) {
                onComplete.accept(success);
            }
        }));
    }
}
