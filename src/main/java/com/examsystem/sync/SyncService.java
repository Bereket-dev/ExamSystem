package com.examsystem.sync;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.db.DeviceRole;
import com.examsystem.model.User;
import com.examsystem.rmi.RMIManager;
import com.examsystem.rmi.remote.SyncBundle;
import com.examsystem.rmi.remote.SyncResult;
import com.examsystem.sync.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates synchronization between the central database (admin) and local backup databases.
 */
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private static SyncService instance;

    private final DatabaseSyncService databaseSyncService = new DatabaseSyncService();
    private ScheduledExecutorService scheduler;
    private volatile boolean autoSyncEnabled;

    private SyncService() {
    }

    public static synchronized SyncService getInstance() {
        if (instance == null) {
            instance = new SyncService();
        }
        return instance;
    }

    public void configureForUser(User user) {
        DeviceRole role = user.getRole() == User.UserRole.ADMIN ? DeviceRole.ADMIN : DeviceRole.CLIENT;
        DatabaseConnection.setDeviceRole(role);
        logger.info("SyncService configured for role {}", role);
    }

    /**
     * Admin: mirror central MySQL into local H2 backup.
     */
    public SyncResult mirrorCentralToBackup() {
        try (Connection central = DatabaseConnection.getCentralConnection();
                Connection backup = DatabaseConnection.getBackupConnection()) {
            SyncBundle bundle = databaseSyncService.exportAll(central);
            return databaseSyncService.importAll(backup, bundle, true);
        } catch (SQLException e) {
            logger.error("Failed to mirror central to backup", e);
            return SyncResult.fail(e.getMessage());
        }
    }

    /**
     * Client: pull authoritative data from admin via RMI into local backup.
     */
    public SyncResult pullFromCentral() {
        RMIManager rmi = RMIManager.getInstance();
        if (!rmi.isClientConnected() && !rmi.connectClient()) {
            return SyncResult.fail("Cannot reach admin RMI server. Working from last local backup.");
        }
        try {
            SyncBundle bundle = rmi.pullSyncBundle();
            if (bundle == null || bundle.getTables().isEmpty()) {
                return SyncResult.fail("Central server returned empty sync data");
            }
            try (Connection backup = DatabaseConnection.getBackupConnection()) {
                return databaseSyncService.importAll(backup, bundle, true);
            }
        } catch (Exception e) {
            logger.error("Pull sync failed", e);
            return SyncResult.fail(e.getMessage());
        }
    }

    /**
     * Client: push local backup changes to central via RMI.
     */
    public SyncResult pushToCentral() {
        RMIManager rmi = RMIManager.getInstance();
        if (!rmi.isClientConnected() && !rmi.connectClient()) {
            return SyncResult.fail("Cannot reach admin RMI server. Changes saved locally only.");
        }
        try (Connection backup = DatabaseConnection.getBackupConnection()) {
            SyncBundle bundle = databaseSyncService.exportAll(backup);
            return rmi.pushSyncBundle(bundle);
        } catch (Exception e) {
            logger.error("Push sync failed", e);
            return SyncResult.fail(e.getMessage());
        }
    }

    /**
     * Full bidirectional sync for client devices when connection is available.
     */
    public SyncResult synchronizeWithCentral() {
        SyncResult pull = pullFromCentral();
        if (!pull.isSuccess()) {
            logger.warn("Pull before push failed: {}", pull.getMessage());
        }
        SyncResult push = pushToCentral();
        if (push.isSuccess()) {
            return push;
        }
        if (pull.isSuccess()) {
            return pull;
        }
        return SyncResult.fail("Sync failed: " + push.getMessage());
    }

    public void startAutoSync() {
        int intervalSec = com.examsystem.util.ConfigManager.getIntProperty("sync.interval.seconds", 120);
        if (intervalSec <= 0 || autoSyncEnabled) {
            return;
        }
        autoSyncEnabled = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "exam-sync");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::runScheduledSync, intervalSec, intervalSec, TimeUnit.SECONDS);
        logger.info("Auto-sync scheduled every {} seconds", intervalSec);
    }

    private void runScheduledSync() {
        try {
            if (DatabaseConnection.isAdminDevice()) {
                mirrorCentralToBackup();
            } else if (RMIManager.getInstance().isClientConnected()) {
                SyncManager syncManager = SyncManager.getInstance();
                if (syncManager.pendingCountProperty().get() > 0) {
                    syncManager.syncNow(false);
                } else {
                    syncManager.syncAuthoritativePull(false);
                }
            }
        } catch (Exception e) {
            logger.warn("Scheduled sync error: {}", e.getMessage());
        }
    }

    public void shutdown() {
        autoSyncEnabled = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (DatabaseConnection.isClientDevice()) {
            try {
                pushToCentral();
            } catch (Exception e) {
                logger.warn("Final push on shutdown failed: {}", e.getMessage());
            }
        } else if (DatabaseConnection.isAdminDevice()) {
            mirrorCentralToBackup();
        }
    }
}
