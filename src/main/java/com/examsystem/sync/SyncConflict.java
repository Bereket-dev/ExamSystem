package com.examsystem.sync;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SyncConflict {
    private final String entityType;
    private final String entityKey;
    private final String localSummary;
    private final String serverSummary;
    private final LocalDateTime localModified;
    private final LocalDateTime serverModified;

    public SyncConflict(String entityType, String entityKey, String localSummary, String serverSummary,
            LocalDateTime localModified, LocalDateTime serverModified) {
        this.entityType = entityType;
        this.entityKey = entityKey;
        this.localSummary = localSummary;
        this.serverSummary = serverSummary;
        this.localModified = localModified;
        this.serverModified = serverModified;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public String getLocalSummary() {
        return localSummary;
    }

    public String getServerSummary() {
        return serverSummary;
    }

    public String getLocalModifiedFormatted() {
        return localModified != null ? localModified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "Unknown";
    }

    public String getServerModifiedFormatted() {
        return serverModified != null ? serverModified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "Unknown";
    }
}
