package com.examsystem.rmi.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializable snapshot of database tables for RMI synchronization.
 */
public class SyncBundle implements Serializable {
    private static final long serialVersionUID = 1L;

    private long timestamp;
    private Map<String, List<Map<String, String>>> tables = new HashMap<>();

    public SyncBundle() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, List<Map<String, String>>> getTables() {
        return tables;
    }

    public void setTables(Map<String, List<Map<String, String>>> tables) {
        this.tables = tables != null ? tables : new HashMap<>();
    }

    public void putTable(String tableName, List<Map<String, String>> rows) {
        tables.put(tableName, rows != null ? rows : new ArrayList<>());
    }

    public List<Map<String, String>> getTable(String tableName) {
        return tables.getOrDefault(tableName, List.of());
    }
}
