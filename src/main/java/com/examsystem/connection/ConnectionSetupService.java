package com.examsystem.connection;

import com.examsystem.db.BackupDatabaseConnection;
import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.sync.SyncManager;
import com.examsystem.sync.SyncService;
import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies online/offline connectivity after login.
 */
public final class ConnectionSetupService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionSetupService.class);

    private ConnectionSetupService() {
    }

    public static ConnectionTestResult testRmiConnection(String host, int port) {
        String trimmedHost = host == null ? "" : host.trim();
        if (trimmedHost.isEmpty()) {
            return ConnectionTestResult.fail("Please enter the admin server IP address.");
        }
        if (port <= 0 || port > 65535) {
            return ConnectionTestResult.fail("RMI port must be between 1 and 65535.");
        }
        try {
            boolean ok = RMIManager.getInstance().testConnection(trimmedHost, port);
            return ok ? ConnectionTestResult.ok() : ConnectionTestResult.fail(
                    "No response from RMI registry at " + trimmedHost + ":" + port + ".");
        } catch (Exception e) {
            logger.warn("RMI test failed: {}", e.getMessage());
            return ConnectionTestResult.fail(SyncManager.friendlyMessage(e));
        }
    }

    /**
     * Applies saved profile and connects. Returns false if online auto-connect fails.
     */
    public static boolean tryApplySavedProfile(ConnectionProfile profile, User user) {
        if (!profile.isValidForAutoConnect()) {
            return false;
        }
        try {
            if (profile.isOfflineMode()) {
                applyOfflineMode(user, profile);
                return true;
            }
            if (user.getRole() == User.UserRole.ADMIN) {
                RMIManager.getInstance().startServer();
            }
            ConnectionTestResult test = testRmiConnection(profile.getServerHost(), profile.getRmiPort());
            if (!test.success()) {
                return false;
            }
            applyOnlineMode(user, profile);
            return true;
        } catch (Exception e) {
            logger.warn("Auto-connect failed: {}", e.getMessage());
            return false;
        }
    }

    public static void applyOnlineMode(User user, ConnectionProfile profile) throws Exception {
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        ConnectionSettingsStore.save(profile);
        DatabaseConnection.setForceOfflineData(false);

        SyncService.getInstance().configureForUser(user);
        connectNetworkServices(user, profile.getServerHost(), profile.getRmiPort(), true);

        SyncManager sync = SyncManager.getInstance();
        sync.initializeForUser(user);
        sync.refreshConnectionState();
        if (sync.isOnline()) {
            sync.syncAuthoritativePull(false);
        }
    }

    public static void applyOfflineMode(User user, ConnectionProfile profile) throws Exception {
        profile.setOfflineMode(true);
        profile.setSetupCompleted(true);
        ConnectionSettingsStore.save(profile);
        DatabaseConnection.setForceOfflineData(true);

        RMIManager.getInstance().disconnectClient();
        RMIManager.getInstance().stopServer();
        NetworkManager.getInstance().disconnectClient();

        if (!BackupDatabaseConnection.testConnection()) {
            throw new IllegalStateException("Local backup database could not be started.");
        }

        SyncService.getInstance().configureForUser(user);
        SyncManager sync = SyncManager.getInstance();
        sync.initializeForUser(user);
        sync.refreshConnectionState();
    }

    private static void connectNetworkServices(User user, String host, int port, boolean startServersForHost)
            throws Exception {
        NetworkManager network = NetworkManager.getInstance();
        RMIManager rmi = RMIManager.getInstance();
        String username = user.getUsername();
        String password = user.getPassword();

        switch (user.getRole()) {
            case ADMIN -> {
                if (startServersForHost) {
                    network.startServer();
                    rmi.startServer();
                    logger.info("Admin: TCP and RMI servers started");
                }
            }
            case TEACHER -> {
                network.startServer();
                logger.info("Teacher: classroom TCP server started");
                if (!rmi.connectClient(host, port)) {
                    throw new IllegalStateException("Could not connect to admin RMI server.");
                }
                rmi.clientLogin(username, password);
            }
            case STUDENT -> {
                boolean tcpOk = network.connectClient();
                if (tcpOk) {
                    network.clientLogin(username, password);
                }
                if (!rmi.connectClient(host, port)) {
                    throw new IllegalStateException("Could not connect to admin RMI server.");
                }
                rmi.clientLogin(username, password);
            }
            default -> throw new IllegalArgumentException("Unsupported role");
        }
    }

    public static ConnectionProfile profileFromInputs(String host, int port) {
        ConnectionProfile profile = new ConnectionProfile();
        profile.setServerHost(host == null ? "localhost" : host.trim());
        profile.setRmiPort(port > 0 ? port : ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        return profile;
    }
}
