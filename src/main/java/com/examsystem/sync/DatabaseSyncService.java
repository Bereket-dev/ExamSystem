package com.examsystem.sync;

import com.examsystem.rmi.remote.SyncBundle;
import com.examsystem.rmi.remote.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exports and imports table data between central MySQL and local H2 backup databases.
 */
public class DatabaseSyncService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSyncService.class);

    /** Import order respects foreign keys. */
    private static final String[] SYNC_TABLES = {
            "users", "teachers", "students", "courses", "teacher_courses",
            "exams", "questions", "options", "student_exam_assignments",
            "exam_attempts", "student_answers"
    };

    /** Delete order is reverse of import. */
    private static final String[] DELETE_ORDER = {
            "student_answers", "exam_attempts", "student_exam_assignments",
            "options", "questions", "exams", "teacher_courses", "courses",
            "students", "teachers", "users"
    };

    public SyncBundle exportAll(Connection source) throws SQLException {
        return exportAll(source, null);
    }

    public SyncResult importAll(Connection target, SyncBundle bundle, boolean replaceExisting) throws SQLException {
        return importAll(target, bundle, replaceExisting, null);
    }

    public SyncResult importAll(Connection target, SyncBundle bundle, boolean replaceExisting,
            SyncProgressListener listener) throws SQLException {
        if (bundle == null || bundle.getTables().isEmpty()) {
            return SyncResult.fail("No sync data received");
        }

        int rowsSynced = 0;
        int tablesSynced = 0;
        boolean mysql = isMySql(target);
        int totalSteps = SYNC_TABLES.length + 2;
        int step = 0;

        target.setAutoCommit(false);
        try {
            if (replaceExisting) {
                reportProgress(listener, "Preparing local backup...", ++step / (double) totalSteps);
                for (String table : DELETE_ORDER) {
                    if (tableExists(target, table)) {
                        try (Statement stmt = target.createStatement()) {
                            stmt.executeUpdate("DELETE FROM " + table);
                        }
                    }
                }
            }

            for (String table : SYNC_TABLES) {
                reportProgress(listener, "Syncing " + table.replace('_', ' ') + "...", ++step / (double) totalSteps);
                List<Map<String, String>> rows = bundle.getTable(table);
                if (rows == null || rows.isEmpty()) {
                    continue;
                }
                if (!tableExists(target, table)) {
                    continue;
                }
                rowsSynced += importTable(target, table, rows, mysql, replaceExisting);
                tablesSynced++;
            }

            reportProgress(listener, "Finalizing synchronization...", ++step / (double) totalSteps);
            updateSyncMetadata(target, bundle.getTimestamp());
            target.commit();
            reportProgress(listener, "Complete", 1.0);
            return SyncResult.ok("Synchronized " + tablesSynced + " tables (" + rowsSynced + " rows)",
                    tablesSynced, rowsSynced);
        } catch (SQLException e) {
            target.rollback();
            throw e;
        } finally {
            target.setAutoCommit(true);
        }
    }

    public SyncBundle exportAll(Connection source, SyncProgressListener listener) throws SQLException {
        SyncBundle bundle = new SyncBundle();
        int total = SYNC_TABLES.length;
        for (int i = 0; i < SYNC_TABLES.length; i++) {
            String table = SYNC_TABLES[i];
            reportProgress(listener, "Reading " + table.replace('_', ' ') + "...", (i + 1) / (double) total);
            if (tableExists(source, table)) {
                bundle.putTable(table, exportTable(source, table));
            }
        }
        bundle.setTimestamp(System.currentTimeMillis());
        reportProgress(listener, "Export complete", 1.0);
        return bundle;
    }

    private void reportProgress(SyncProgressListener listener, String label, double fraction) {
        if (listener != null) {
            listener.onProgress(label, Math.min(1.0, Math.max(0, fraction)));
        }
    }

    private List<Map<String, String>> exportTable(Connection conn, String table) throws SQLException {
        List<Map<String, String>> rows = new ArrayList<>();
        String sql = "SELECT * FROM " + table;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String col = meta.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(col, val == null ? null : String.valueOf(val));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private int importTable(Connection conn, String table, List<Map<String, String>> rows,
            boolean mysql, boolean replace) throws SQLException {
        if (rows.isEmpty()) {
            return 0;
        }

        Map<String, String> first = rows.get(0);
        List<String> columns = new ArrayList<>(first.keySet());

        StringBuilder colList = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                colList.append(", ");
                placeholders.append(", ");
            }
            colList.append(columns.get(i));
            placeholders.append("?");
        }

        String insertSql;
        if (mysql && !replace) {
            StringBuilder updateClause = new StringBuilder();
            for (String col : columns) {
                if (updateClause.length() > 0) {
                    updateClause.append(", ");
                }
                updateClause.append(col).append("=VALUES(").append(col).append(")");
            }
            insertSql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders
                    + ") ON DUPLICATE KEY UPDATE " + updateClause;
        } else {
            insertSql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")";
        }

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Map<String, String> row : rows) {
                for (int i = 0; i < columns.size(); i++) {
                    setParameter(stmt, i + 1, row.get(columns.get(i)));
                }
                stmt.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private void setParameter(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || "null".equalsIgnoreCase(value)) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }

    private void updateSyncMetadata(Connection conn, long timestamp) {
        if (!tableExists(conn, "sync_metadata")) {
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "MERGE INTO sync_metadata (meta_key, meta_value) KEY(meta_key) VALUES ('last_sync', ?)")) {
            stmt.setString(1, String.valueOf(timestamp));
            stmt.executeUpdate();
        } catch (SQLException e) {
            try (PreparedStatement upsert = conn.prepareStatement(
                    "INSERT INTO sync_metadata (meta_key, meta_value) VALUES ('last_sync', ?) "
                            + "ON CONFLICT (meta_key) DO UPDATE SET meta_value = EXCLUDED.meta_value")) {
                upsert.setString(1, String.valueOf(timestamp));
                upsert.executeUpdate();
            } catch (SQLException ignored) {
                logger.debug("Could not update sync_metadata: {}", e.getMessage());
            }
        }
    }

    private boolean tableExists(Connection conn, String table) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, table.toUpperCase(), new String[] { "TABLE" })) {
                if (rs.next()) {
                    return true;
                }
            }
            try (ResultSet rs = meta.getTables(null, null, table.toLowerCase(), new String[] { "TABLE" })) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isMySql(Connection conn) throws SQLException {
        String product = conn.getMetaData().getDatabaseProductName();
        return product != null && product.toLowerCase().contains("mysql");
    }
}
