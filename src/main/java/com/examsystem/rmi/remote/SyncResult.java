package com.examsystem.rmi.remote;

import java.io.Serializable;

/**
 * Result of a push/pull sync operation over RMI.
 */
public class SyncResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private int tablesSynced;
    private int rowsSynced;

    public SyncResult() {
    }

    public SyncResult(boolean success, String message, int tablesSynced, int rowsSynced) {
        this.success = success;
        this.message = message;
        this.tablesSynced = tablesSynced;
        this.rowsSynced = rowsSynced;
    }

    public static SyncResult ok(String message, int tablesSynced, int rowsSynced) {
        return new SyncResult(true, message, tablesSynced, rowsSynced);
    }

    public static SyncResult fail(String message) {
        return new SyncResult(false, message, 0, 0);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getTablesSynced() {
        return tablesSynced;
    }

    public int getRowsSynced() {
        return rowsSynced;
    }
}
