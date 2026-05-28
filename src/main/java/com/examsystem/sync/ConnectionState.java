package com.examsystem.sync;

/**
 * Real-time connectivity state for distributed sync UX.
 */
public enum ConnectionState {
    ONLINE("Connected to Central Server", "sync-status-online"),
    SYNCING("Syncing...", "sync-status-syncing"),
    OFFLINE("Offline Mode", "sync-status-offline"),
    RECONNECTING("Reconnecting to central server...", "sync-status-reconnecting");

    private final String displayText;
    private final String styleClass;

    ConnectionState(String displayText, String styleClass) {
        this.displayText = displayText;
        this.styleClass = styleClass;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getIndicatorEmoji() {
        return switch (this) {
            case ONLINE -> "🟢";
            case SYNCING -> "🟡";
            case OFFLINE -> "🔴";
            case RECONNECTING -> "🟡";
        };
    }
}
