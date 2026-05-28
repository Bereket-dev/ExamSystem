package com.examsystem.connection;

import com.examsystem.db.BackupDatabaseConnection;
import com.examsystem.db.DatabaseConnection;
import com.examsystem.db.DeviceRole;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.rmi.remote.ClientPresenceResult;
import com.examsystem.sync.ClientSessionRegistry;
import com.examsystem.sync.SyncManager;
import com.examsystem.sync.SyncService;
import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies online/offline connectivity after login (role-aware).
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
                    "Could not reach the admin server. Check the IP, port, and that the admin app is running.");
        } catch (Exception e) {
            logger.warn("RMI test failed: {}", e.getMessage());
            return ConnectionTestResult.fail(SyncManager.friendlyMessage(e));
        }
    }

    /**
     * Restores a saved profile after login. Never throws.
     */
    public static boolean tryApplySavedProfile(ConnectionProfile profile, User user) {
        if (!profile.isSetupCompleted()) {
            return false;
        }
        try {
            if (profile.isOfflineMode()) {
                applyOfflineModeResilient(user, profile);
                return true;
            }
            if (user.getRole() == User.UserRole.ADMIN) {
                applyAdminServerMode(user, profile);
                return true;
            }
            return tryApplyClientOnline(profile, user);
        } catch (Exception e) {
            logger.warn("Auto-connect failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Teacher/student online reconnect using saved IP/port.
     */
    public static boolean tryApplyClientOnline(ConnectionProfile profile, User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return false;
        }
        ConnectionTestResult test = testRmiConnection(profile.getServerHost(), profile.getRmiPort());
        if (!test.success()) {
            return false;
        }
        try {
            applyClientOnlineMode(user, profile);
            return true;
        } catch (Exception e) {
            logger.warn("Client online apply failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Sync point: admin UI polls for clients. Called AFTER applyAdminServerMode() has already
     * put the registry in waiting state.
     */
    public static void beginAdminWaitForClient() {
        // Already in waiting state from applyAdminServerMode(); this is just for sync
        logger.debug("Admin UI now polling for client registrations");
    }

    /** Admin device: start TCP + RMI registry (no remote client connection). */
    public static void applyAdminServerMode(User user, ConnectionProfile profile) throws Exception {
        int port = profile.getRmiPort() > 0
                ? profile.getRmiPort()
                : ConfigManager.getIntProperty("rmi.registry.port", 1099);

        profile.setServerHost("localhost");
        profile.setRmiPort(port);
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        ConnectionSettingsStore.save(profile);

        DatabaseConnection.setDeviceRole(DeviceRole.ADMIN);
        DatabaseConnection.setForceOfflineData(false);

        ConfigManager.setRuntimeProperty("rmi.registry.host", "localhost");
        ConfigManager.setRuntimeProperty("rmi.registry.port", String.valueOf(port));

        // CRITICAL: Enter waiting state BEFORE starting server so client registrations are captured
        ClientSessionRegistry.getInstance().beginWaitingForClient();
        logger.debug("Admin waiting state ENABLED before server start");

        NetworkManager network = NetworkManager.getInstance();
        RMIManager rmi = RMIManager.getInstance();
        network.startServer();
        rmi.startServer();
        logger.info("Admin server mode: TCP and RMI started on port {} (already waiting for clients)", port);

        SyncService.getInstance().configureForUser(user);
        SyncManager sync = SyncManager.getInstance();
        sync.initializeForUser(user);
        sync.refreshConnectionState();
        try {
            if (sync.isOnline()) {
                sync.syncAuthoritativePull(false);
            }
        } catch (Exception e) {
            logger.warn("Initial admin backup sync skipped: {}", e.getMessage());
        }
    }

    /** Teacher/student online: connect as RMI client to admin. */
    public static void applyClientOnlineMode(User user, ConnectionProfile profile) throws Exception {
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        ConnectionSettingsStore.save(profile);
        DatabaseConnection.setForceOfflineData(false);

        ConfigManager.setRuntimeProperty("rmi.registry.host", profile.getServerHost());
        ConfigManager.setRuntimeProperty("rmi.registry.port", String.valueOf(profile.getRmiPort()));

        SyncService.getInstance().configureForUser(user);
        connectClientNetworkServices(user, profile.getServerHost(), profile.getRmiPort());

        SyncManager sync = SyncManager.getInstance();
        sync.initializeForUser(user);
        sync.refreshConnectionState();
        try {
            if (sync.isOnline()) {
                sync.syncAuthoritativePull(false);
            }
        } catch (Exception e) {
            logger.warn("Initial client sync skipped: {}", e.getMessage());
        }
    }

    /**
     * Offline mode for any role — always completes; never blocks dashboard access.
     */
    public static void applyOfflineModeResilient(User user, ConnectionProfile profile) {
        try {
            profile.setOfflineMode(true);
            profile.setSetupCompleted(true);
            if (profile.getServerHost() == null || profile.getServerHost().isBlank()) {
                profile.setServerHost("localhost");
            }
            if (profile.getRmiPort() <= 0) {
                profile.setRmiPort(ConfigManager.getIntProperty("rmi.registry.port", 1099));
            }
            ConnectionSettingsStore.save(profile);
        } catch (Exception e) {
            logger.warn("Could not persist offline settings: {}", e.getMessage());
            ConnectionSettingsStore.applyToRuntime(profile);
        }

        DatabaseConnection.setForceOfflineData(true);
        SyncService.getInstance().configureForUser(user);

        try {
            RMIManager.getInstance().disconnectClient();
        } catch (Exception ignored) {
        }
        if (user.getRole() != User.UserRole.ADMIN) {
            try {
                RMIManager.getInstance().stopServer();
            } catch (Exception ignored) {
            }
        }
        try {
            NetworkManager.getInstance().disconnectClient();
            if (user.getRole() != User.UserRole.ADMIN) {
                NetworkManager.getInstance().stopServer();
            }
        } catch (Exception ignored) {
        }

        try {
            BackupDatabaseConnection.testConnection();
        } catch (Exception e) {
            logger.warn("Backup DB init warning (offline mode continues): {}", e.getMessage());
        }

        try {
            SyncManager sync = SyncManager.getInstance();
            sync.initializeForUser(user);
            sync.refreshConnectionState();
        } catch (Exception e) {
            logger.warn("Sync manager init in offline mode: {}", e.getMessage());
        }
        logger.info("Offline mode active for {} ({})", user.getUsername(), user.getRole());
    }

    private static void connectClientNetworkServices(User user, String host, int port) throws Exception {
        NetworkManager network = NetworkManager.getInstance();
        RMIManager rmi = RMIManager.getInstance();
        String username = user.getUsername();
        String password = user.getPassword();

        logger.info("[CLIENT-CONNECT] Starting network connection for {} to {}:{}", username, host, port);

        switch (user.getRole()) {
            case TEACHER -> {
                try {
                    network.startServer();
                } catch (Exception e) {
                    logger.warn("Teacher TCP server not started: {}", e.getMessage());
                }
                logger.debug("[CLIENT-CONNECT] Attempting RMI client connection");
                if (!rmi.connectClient(host, port)) {
                    throw new IllegalStateException("Could not connect to the admin RMI server.");
                }
                logger.info("[CLIENT-CONNECT] RMI client connected, logging in");
                rmi.clientLogin(username, password);
                logger.info("[CLIENT-CONNECT] Login successful, notifying admin of presence");
                notifyAdminOfClientPresence(user);
                logger.info("[CLIENT-CONNECT] Presence notification complete");
            }
            case STUDENT -> {
                try {
                    if (network.connectClient()) {
                        network.clientLogin(username, password);
                    }
                } catch (Exception e) {
                    logger.warn("Student TCP connect skipped: {}", e.getMessage());
                }
                logger.debug("[CLIENT-CONNECT] Attempting RMI client connection");
                if (!rmi.connectClient(host, port)) {
                    throw new IllegalStateException("Could not connect to the admin RMI server.");
                }
                logger.info("[CLIENT-CONNECT] RMI client connected, logging in");
                rmi.clientLogin(username, password);
                logger.info("[CLIENT-CONNECT] Login successful, notifying admin of presence");
                notifyAdminOfClientPresence(user);
                logger.info("[CLIENT-CONNECT] Presence notification complete");
            }
            default -> throw new IllegalArgumentException("Only teachers and students use client online mode");
        }
    }

    private static void notifyAdminOfClientPresence(User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return;
        }
        logger.info("[PRESENCE-NOTIFY] Registering {} presence with admin server", user.getUsername());
        // Presence registration is REQUIRED — if it fails, the connection setup fails
        ClientPresenceResult result = RMIManager.getInstance().registerClientPresence(
                user.getUsername(), user.getRole().name());
        logger.info("[PRESENCE-NOTIFY] Registration result: success={}, message={}", result.isSuccess(), result.getMessage());
        if (result.isSuccess()) {
            logger.info("[PRESENCE-NOTIFY] Admin notified of client presence successfully");
            return;
        }
        logger.error("[PRESENCE-NOTIFY] Registration FAILED: {}", result.getMessage());
        throw new IllegalStateException(
                "Failed to register with admin server. The admin may not be running or accepting connections. "
                        + "Details: " + result.getMessage());
    }

    public static ConnectionProfile profileFromInputs(String host, int port) {
        ConnectionProfile profile = new ConnectionProfile();
        profile.setServerHost(host == null || host.isBlank() ? "localhost" : host.trim());
        profile.setRmiPort(port > 0 ? port : ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        return profile;
    }

    public static ConnectionProfile adminServerProfile() {
        ConnectionProfile profile = new ConnectionProfile();
        profile.setServerHost("localhost");
        profile.setRmiPort(ConfigManager.getIntProperty("rmi.registry.port", 1099));
        profile.setOfflineMode(false);
        profile.setSetupCompleted(true);
        return profile;
    }
}
