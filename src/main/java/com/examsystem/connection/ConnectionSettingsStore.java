package com.examsystem.connection;

import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Persists RMI / offline settings on the local device (survives restarts).
 */
public final class ConnectionSettingsStore {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionSettingsStore.class);

    private static final String KEY_SETUP_COMPLETED = "connection.setup.completed";
    private static final String KEY_SERVER_HOST = "rmi.registry.host";
    private static final String KEY_RMI_PORT = "rmi.registry.port";
    private static final String KEY_OFFLINE_MODE = "connection.offline.mode";

    private ConnectionSettingsStore() {
    }

    public static Path getSettingsPath() {
        String configured = ConfigManager.getProperty("connection.settings.path", "data/connection.properties");
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    public static ConnectionProfile load() {
        ConnectionProfile profile = new ConnectionProfile();
        profile.setServerHost(ConfigManager.getProperty("rmi.registry.host", "localhost"));
        profile.setRmiPort(ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(ConfigManager.getBooleanProperty("connection.offline.mode", false));
        profile.setSetupCompleted(ConfigManager.getBooleanProperty("connection.setup.completed", false));

        Path path = getSettingsPath();
        if (!Files.isRegularFile(path)) {
            return profile;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            if (props.containsKey(KEY_SERVER_HOST)) {
                profile.setServerHost(props.getProperty(KEY_SERVER_HOST, "localhost").trim());
            }
            if (props.containsKey(KEY_RMI_PORT)) {
                profile.setRmiPort(parsePort(props.getProperty(KEY_RMI_PORT), profile.getRmiPort()));
            }
            if (props.containsKey(KEY_OFFLINE_MODE)) {
                profile.setOfflineMode(Boolean.parseBoolean(props.getProperty(KEY_OFFLINE_MODE)));
            }
            if (props.containsKey(KEY_SETUP_COMPLETED)) {
                profile.setSetupCompleted(Boolean.parseBoolean(props.getProperty(KEY_SETUP_COMPLETED)));
            }
            applyToRuntime(profile);
            logger.info("Loaded connection settings from {}", path);
        } catch (IOException e) {
            logger.warn("Could not read connection settings: {}", e.getMessage());
        }
        return profile;
    }

    public static void save(ConnectionProfile profile) throws IOException {
        Path path = getSettingsPath();
        Files.createDirectories(path.getParent());
        Properties props = new Properties();
        props.setProperty(KEY_SETUP_COMPLETED, String.valueOf(profile.isSetupCompleted()));
        props.setProperty(KEY_SERVER_HOST, profile.getServerHost());
        props.setProperty(KEY_RMI_PORT, String.valueOf(profile.getRmiPort()));
        props.setProperty(KEY_OFFLINE_MODE, String.valueOf(profile.isOfflineMode()));
        try (OutputStream out = Files.newOutputStream(path)) {
            props.store(out, "ExamSystem connection settings");
        }
        applyToRuntime(profile);
        logger.info("Saved connection settings to {}", path);
    }

    public static void applyToRuntime(ConnectionProfile profile) {
        ConfigManager.setRuntimeProperty(KEY_SETUP_COMPLETED, String.valueOf(profile.isSetupCompleted()));
        ConfigManager.setRuntimeProperty(KEY_SERVER_HOST, profile.getServerHost());
        ConfigManager.setRuntimeProperty(KEY_RMI_PORT, String.valueOf(profile.getRmiPort()));
        ConfigManager.setRuntimeProperty(KEY_OFFLINE_MODE, String.valueOf(profile.isOfflineMode()));
    }

    private static int parsePort(String value, int defaultPort) {
        try {
            int port = Integer.parseInt(value.trim());
            return port > 0 && port <= 65535 ? port : defaultPort;
        } catch (NumberFormatException e) {
            return defaultPort;
        }
    }
}
