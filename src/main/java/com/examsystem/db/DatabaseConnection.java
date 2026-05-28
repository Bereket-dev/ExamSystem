package com.examsystem.db;

import com.examsystem.util.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        return ConfigManager.getProperty("db.password", "password");
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
            runSchemaMigrationIfNeeded();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new SQLException(
                    "Cannot connect to MySQL. Run: sudo mysql < src/main/resources/sql/setup_user.sql",
                    e);
        }
    }

    private static void runSchemaMigrationIfNeeded() {
        try (Connection conn = dataSource.getConnection()) {
            if (!doesTableExist(conn, "exams")) {
                logger.warn("ExamSystem database table 'exams' does not exist. Ensure schema.sql has been applied.");
                return;
            }

            if (!doesColumnExist(conn, "exams", "course_id")) {
                logger.info("Applying missing course_id migration to exams table");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE exams ADD COLUMN course_id INT NULL AFTER subject");
                    stmt.executeUpdate(
                            "ALTER TABLE exams ADD CONSTRAINT fk_exams_course_id FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL");
                    stmt.executeUpdate("CREATE INDEX idx_exam_course ON exams(course_id)");
                }
                logger.info("Migration completed: added course_id column to exams table");
            } else {
                logger.info("Database schema verified: exams.course_id is present");
            }
        } catch (SQLException e) {
            logger.error("Schema migration check failed", e);
        }
    }

    private static boolean doesTableExist(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, getDbName());
            stmt.setString(2, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean doesColumnExist(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, getDbName());
            stmt.setString(2, tableName);
            stmt.setString(3, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
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
