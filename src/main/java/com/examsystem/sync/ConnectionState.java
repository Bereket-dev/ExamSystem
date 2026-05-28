package com.examsystem.sync;

/**
 * Real-time connectivity state for distributed sync UX.
 */
public enum ConnectionState {
    ONLINE("RMI Connected (Online)", "sync-dot-online"),
    SYNCING("Syncing...", "sync-dot-syncing"),
    OFFLINE("Offline Mode", "sync-dot-offline"),
    RECONNECTING("Reconnecting...", "sync-dot-reconnecting");

    private final String displayText;
    private final String dotStyleClass;

    ConnectionState(String displayText, String dotStyleClass) {
        this.displayText = displayText;
        this.dotStyleClass = dotStyleClass;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getDotStyleClass() {
        return dotStyleClass;
    }
}
