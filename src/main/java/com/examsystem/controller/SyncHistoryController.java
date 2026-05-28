package com.examsystem.controller;

import com.examsystem.sync.SyncHistoryEntry;
import com.examsystem.sync.SyncManager;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SyncHistoryController {

    @FXML
    private TableView<SyncHistoryEntry> historyTable;

    @FXML
    public void initialize() {
        historyTable.setItems(SyncManager.getInstance().getHistoryStore().getEntries());
        addColumn("Timestamp", "timestampFormatted");
        addColumn("Type", "syncTypeLabel");
        addColumn("Role", "deviceRole");
        addColumn("Status", "statusLabel");
        addColumn("Message", "message");
        addColumn("Error", "errorDetail");
    }

    private void addColumn(String title, String property) {
        TableColumn<SyncHistoryEntry, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(title.equals("Message") ? 200 : 110);
        historyTable.getColumns().add(col);
    }
}
