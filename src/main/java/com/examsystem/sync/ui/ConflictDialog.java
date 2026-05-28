package com.examsystem.sync.ui;

import com.examsystem.sync.SyncConflict;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public final class ConflictDialog {

    public enum Resolution {
        KEEP_LOCAL,
        KEEP_SERVER
    }

    private ConflictDialog() {
    }

    public static void show(Stage owner, SyncConflict conflict, Consumer<Resolution> onResolved) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sync Conflict");
        alert.setHeaderText("Conflicting updates detected for " + conflict.getEntityType()
                + " (" + conflict.getEntityKey() + ")");

        Label local = new Label("Local: " + conflict.getLocalSummary() + "\nModified: "
                + conflict.getLocalModifiedFormatted());
        local.setWrapText(true);
        Label server = new Label("Server: " + conflict.getServerSummary() + "\nModified: "
                + conflict.getServerModifiedFormatted());
        server.setWrapText(true);
        VBox content = new VBox(10, local, server);
        content.setPadding(new Insets(8));
        alert.getDialogPane().setContent(content);

        ButtonType keepLocal = new ButtonType("Keep Local", ButtonBar.ButtonData.LEFT);
        ButtonType keepServer = new ButtonType("Keep Server", ButtonBar.ButtonData.RIGHT);
        alert.getButtonTypes().setAll(keepLocal, keepServer, ButtonType.CANCEL);

        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait().ifPresent(choice -> {
            if (choice == keepLocal) {
                onResolved.accept(Resolution.KEEP_LOCAL);
            } else if (choice == keepServer) {
                onResolved.accept(Resolution.KEEP_SERVER);
            }
        });
    }
}
