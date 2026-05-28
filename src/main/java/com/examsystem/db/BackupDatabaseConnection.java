package com.examsystem.db;

import com.examsystem.util.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Embedded H2 backup database for offline access and recovery on client devices.
 */
public final class BackupDatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(BackupDatabaseConnection.class);
    private static volatile HikariDataSource backupDataSource;
    private static final Object LOCK = new Object();

    private BackupDatabaseConnection() {
    }

    private static String getBackupPath() {
        return ConfigManager.getProperty("db.backup.path", "./data/exam_backup");
    }

    private static HikariDataSource getDataSource() throws SQLException {
        if (backupDataSource == null) {
            synchronized (LOCK) {
                if (backupDataSource == null) {
                    initializeDataSource();
                }
            }
        }
        return backupDataSource;
    }

    private static void initializeDataSource() throws SQLException {
        try {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = "jdbc:h2:" + getBackupPath() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
            config.setJdbcUrl(jdbcUrl);
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setPoolName("ExamBackupPool");

            backupDataSource = new HikariDataSource(config);
            runSchemaScript();
            logger.info("Backup database initialized at {}", getBackupPath());
        } catch (Exception e) {
            throw new SQLException("Failed to initialize backup database: " + e.getMessage(), e);
        }
    }

    private static void runSchemaScript() throws SQLException {
        try (InputStream in = BackupDatabaseConnection.class.getResourceAsStream("/sql/backup_schema_h2.sql")) {
            if (in == null) {
                logger.warn("backup_schema_h2.sql not found");
                return;
            }
            String script = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            try (Connection conn = backupDataSource.getConnection();
                    Statement stmt = conn.createStatement()) {
                for (String sql : script.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to apply backup schema", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.error("Backup database connection test failed", e);
            return false;
        }
    }

    public static void closePool() {
        if (backupDataSource != null && !backupDataSource.isClosed()) {
            backupDataSource.close();
            backupDataSource = null;
            logger.info("Backup database pool closed");
        }
    }
}
