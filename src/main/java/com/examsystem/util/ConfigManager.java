package com.examsystem.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration manager to load and access application properties.
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    /**
     * Load properties from application.properties file
     */
    private static void loadProperties() {
        try (InputStream input = ConfigManager.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                logger.warn("application.properties file not found in resources");
                return;
            }
            properties.load(input);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading configuration file", e);
        }
    }

    /**
     * Get property value as String
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get property value as String
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property value as Integer
     */
    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property: " + key, e);
            return defaultValue;
        }
    }

    /**
     * Get property value as Boolean
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}
