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
 * Central MySQL database (admin primary store) and routing for client backup access.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static volatile HikariDataSource centralDataSource;
    private static volatile DeviceRole deviceRole = DeviceRole.fromConfig(
            ConfigManager.getProperty("device.role", "admin"));
    private static volatile boolean forceOfflineData;
    private static final Object LOCK = new Object();

    public static void setForceOfflineData(boolean offline) {
        forceOfflineData = offline;
        logger.info("Force offline data mode: {}", offline);
    }

    public static boolean isForceOfflineData() {
        return forceOfflineData;
    }

    public static void setDeviceRole(DeviceRole role) {
        deviceRole = role;
        logger.info("Device role set to {}", role);
    }

    public static DeviceRole getDeviceRole() {
        return deviceRole;
    }

    public static boolean isAdminDevice() {
        return deviceRole == DeviceRole.ADMIN;
    }

    public static boolean isClientDevice() {
        return deviceRole == DeviceRole.CLIENT;
    }

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

    private static HikariDataSource getCentralDataSource() throws SQLException {
        if (centralDataSource == null) {
            synchronized (LOCK) {
                if (centralDataSource == null) {
                    initializeCentralDataSource();
                }
            }
        }
        return centralDataSource;
    }

    private static void initializeCentralDataSource() throws SQLException {
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
            config.setPoolName("ExamCentralPool");

            centralDataSource = new HikariDataSource(config);
            logger.info("Central database pool initialized for '{}' on '{}'", getDbUser(), getDbName());
            runSchemaMigrationIfNeeded();
        } catch (Exception e) {
            logger.error("Failed to initialize central database connection pool", e);
            throw new SQLException(
                    "Cannot connect to MySQL. Run: sudo mysql < src/main/resources/sql/setup_user.sql",
                    e);
        }
    }

    private static void runSchemaMigrationIfNeeded() {
        try (Connection conn = centralDataSource.getConnection()) {
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

    /**
     * Primary connection for application repositories.
     * Admin devices use central MySQL; client devices use local H2 backup.
     */
    public static Connection getConnection() throws SQLException {
        if (isClientDevice() || forceOfflineData) {
            return BackupDatabaseConnection.getConnection();
        }
        return getCentralConnection();
    }

    /** Central authoritative MySQL database (admin host). */
    public static Connection getCentralConnection() throws SQLException {
        return getCentralDataSource().getConnection();
    }

    /** Authentication and user master data always use the central database. */
    public static Connection getAuthConnection() throws SQLException {
        return getCentralConnection();
    }

    /** Local H2 backup database. */
    public static Connection getBackupConnection() throws SQLException {
        return BackupDatabaseConnection.getConnection();
    }

    public static boolean testConnection() {
        try {
            if (isClientDevice()) {
                return BackupDatabaseConnection.testConnection();
            }
            try (Connection conn = getCentralConnection()) {
                return conn.isValid(2);
            }
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    public static boolean testCentralConnection() {
        try (Connection conn = getCentralConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.error("Central database connection test failed", e);
            return false;
        }
    }

    public static void closePool() {
        closeCentralPool();
        BackupDatabaseConnection.closePool();
    }

    public static void closeCentralPool() {
        if (centralDataSource != null && !centralDataSource.isClosed()) {
            centralDataSource.close();
            centralDataSource = null;
            logger.info("Central database connection pool closed");
        }
    }
}
