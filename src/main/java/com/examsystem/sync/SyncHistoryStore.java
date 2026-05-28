package com.examsystem.sync;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SyncHistoryStore {
    private static final int MAX_ENTRIES = 200;
    private final ObservableList<SyncHistoryEntry> entries = FXCollections.observableArrayList();

    public ObservableList<SyncHistoryEntry> getEntries() {
        return entries;
    }

    public void add(SyncType type, String deviceRole, boolean success, String message, String errorDetail) {
        SyncHistoryEntry entry = new SyncHistoryEntry(LocalDateTime.now(), type, deviceRole, success, message,
                errorDetail);
        javafx.application.Platform.runLater(() -> {
            entries.add(0, entry);
            while (entries.size() > MAX_ENTRIES) {
                entries.remove(entries.size() - 1);
            }
        });
    }

    public SyncHistoryEntry getLastSuccessful() {
        for (SyncHistoryEntry e : entries) {
            if (e.isSuccess()) {
                return e;
            }
        }
        return null;
    }

    public List<SyncHistoryEntry> snapshot() {
        return new ArrayList<>(entries);
    }
}
