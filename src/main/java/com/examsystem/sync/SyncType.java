package com.examsystem.sync;

public enum SyncType {
    PULL("Pull"),
    PUSH("Push"),
    AUTO("Auto"),
    MIRROR("Backup"),
    MANUAL("Manual");

    private final String label;

    SyncType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
