package com.examsystem.sync;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SyncHistoryEntry {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final SyncType syncType;
    private final String deviceRole;
    private final boolean success;
    private final String message;
    private final String errorDetail;

    public SyncHistoryEntry(LocalDateTime timestamp, SyncType syncType, String deviceRole,
            boolean success, String message, String errorDetail) {
        this.timestamp = timestamp;
        this.syncType = syncType;
        this.deviceRole = deviceRole;
        this.success = success;
        this.message = message;
        this.errorDetail = errorDetail;
    }

    public String getTimestampFormatted() {
        return timestamp.format(FMT);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public String getSyncTypeLabel() {
        return syncType.getLabel();
    }

    public String getDeviceRole() {
        return deviceRole;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatusLabel() {
        return success ? "Success" : "Failed";
    }

    public String getMessage() {
        return message;
    }

    public String getErrorDetail() {
        return errorDetail != null ? errorDetail : "";
    }
}
