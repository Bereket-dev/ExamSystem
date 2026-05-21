package com.examsystem.db;

import com.examsystem.util.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Connection Manager using HikariCP connection pooling.
 * Manages MySQL database connections for the ExamSystem.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private static String getDbHost() {
        return ConfigManager.getProperty("db.host", "localhost");
    }

    private static int getDbPort() {
        return ConfigManager.getIntProperty("db.port", 3306);
    }

    private static String getDbName() {
        return ConfigManager.getProperty("db.name", "exam_system");
    }

    private static String getDbUser() {
        return ConfigManager.getProperty("db.user", "examsystem");
    }

    private static String getDbPassword() {
        return ConfigManager.getProperty("db.password", "1234");
    }

    private static HikariDataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    initializeDataSource();
                }
            }
        }
        return dataSource;
    }

    private static void initializeDataSource() throws SQLException {
        try {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?allowPublicKeyRetrieval=true&useSSL=false",
                    getDbHost(), getDbPort(), getDbName());
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(getDbUser());
            config.setPassword(getDbPassword());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(20000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(1200000);
            config.setAutoCommit(true);

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized for user '{}' on '{}'",
                    getDbUser(), getDbName());
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new SQLException(
                    "Cannot connect to MySQL. Run: sudo mysql < src/main/resources/sql/setup_user.sql",
                    e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Database connection test successful");
            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            logger.info("Database connection pool closed");
        }
    }
}
