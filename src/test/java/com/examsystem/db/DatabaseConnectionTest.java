package com.examsystem.db;

import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Unit tests for DatabaseConnection class.
 * Tests connection pool initialization and connectivity.
 */
public class DatabaseConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    @Test
    public void testDatabaseConnection() {
        logger.info("Testing database connection...");
        assertTrue("Database connection should be established",
                DatabaseConnection.testConnection());
    }

    @Test
    public void testGetConnection() {
        logger.info("Testing getConnection method...");
        try {
            Connection connection = DatabaseConnection.getConnection();
            assertNotNull("Connection should not be null", connection);
            assertFalse("Connection should not be closed", connection.isClosed());
            connection.close();
            logger.info("Connection test passed");
        } catch (SQLException e) {
            logger.error("Connection test failed", e);
            fail("Failed to get database connection: " + e.getMessage());
        }
    }

    @Test
    public void testConnectionPooling() {
        logger.info("Testing connection pooling...");
        try {
            // Get multiple connections
            Connection conn1 = DatabaseConnection.getConnection();
            Connection conn2 = DatabaseConnection.getConnection();

            assertNotNull(conn1);
            assertNotNull(conn2);

            // Connections should be different instances
            assertNotSame("Different connections should be obtained", conn1, conn2);

            conn1.close();
            conn2.close();

            logger.info("Connection pooling test passed");
        } catch (SQLException e) {
            logger.error("Connection pooling test failed", e);
            fail("Failed connection pooling test: " + e.getMessage());
        }
    }
}
