package com.examsystem.sync;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.db.DeviceRole;
import com.examsystem.model.User;
import com.examsystem.rmi.RMIManager;
import com.examsystem.rmi.remote.SyncBundle;
import com.examsystem.rmi.remote.SyncResult;
import com.examsystem.util.ConfigManager;
import com.examsystem.util.Session;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Centralized sync orchestration with JavaFX observable state for UI binding.
 */
public class SyncManager {
    private static final Logger logger = LoggerFactory.getLogger(SyncManager.class);
    private static final String MSG_SYNCED_WITH_SERVER = "Successfully synced with central server";
    private static SyncManager instance;

    private final SyncService syncService = SyncService.getInstance();
    private final DatabaseSyncService databaseSyncService = new DatabaseSyncService();
    private final PendingChangesQueue pendingQueue = new PendingChangesQueue();
    private final SyncHistoryStore historyStore = new SyncHistoryStore();
    private final ConflictDetector conflictDetector = new ConflictDetector();

    private final ObjectProperty<ConnectionState> connectionState = new SimpleObjectProperty<>(ConnectionState.OFFLINE);
    private final StringProperty lastSyncTime = new SimpleStringProperty("Never");
    private final StringProperty databaseSource = new SimpleStringProperty("—");
    private final StringProperty syncMode = new SimpleStringProperty("Auto");
    private final StringProperty currentOperation = new SimpleStringProperty("Idle");
    private final StringProperty topologyText = new SimpleStringProperty("");
    private final DoubleProperty syncProgress = new SimpleDoubleProperty(0);
    private final BooleanProperty syncInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty offlineBannerVisible = new SimpleBooleanProperty(false);
    private final StringProperty deviceRoleLabel = new SimpleStringProperty("");

    private final IntegerProperty connectedClients = new SimpleIntegerProperty(0);
    private final IntegerProperty offlineClients = new SimpleIntegerProperty(0);
    private final IntegerProperty activeExamsCount = new SimpleIntegerProperty(0);
    private final StringProperty lastSyncActivity = new SimpleStringProperty("—");

    private ConnectionMonitor connectionMonitor;
    private Stage primaryStage;
    private User currentUser;
    private final AtomicBoolean manualSyncMode = new AtomicBoolean(false);
    private volatile boolean lastSyncWasAuthoritativePull;

    private SyncManager() {
        connectionState.addListener((obs, old, neu) -> {
            offlineBannerVisible.set(neu == ConnectionState.OFFLINE);
            updateTopology();
        });
    }

    public static synchronized SyncManager getInstance() {
        if (instance == null) {
            instance = new SyncManager();
        }
        return instance;
    }

    // --- Observable accessors ---

    public ObjectProperty<ConnectionState> connectionStateProperty() {
        return connectionState;
    }

    public ReadOnlyIntegerProperty pendingCountProperty() {
        return pendingQueue.pendingCountProperty();
    }

    public StringProperty lastSyncTimeProperty() {
        return lastSyncTime;
    }

    public StringProperty databaseSourceProperty() {
        return databaseSource;
    }

    public StringProperty syncModeProperty() {
        return syncMode;
    }

    public StringProperty currentOperationProperty() {
        return currentOperation;
    }

    public StringProperty topologyTextProperty() {
        return topologyText;
    }

    public DoubleProperty syncProgressProperty() {
        return syncProgress;
    }

    public BooleanProperty syncInProgressProperty() {
        return syncInProgress;
    }

    public BooleanProperty offlineBannerVisibleProperty() {
        return offlineBannerVisible;
    }

    public StringProperty deviceRoleLabelProperty() {
        return deviceRoleLabel;
    }

    public IntegerProperty connectedClientsProperty() {
        return connectedClients;
    }

    public IntegerProperty offlineClientsProperty() {
        return offlineClients;
    }

    public IntegerProperty activeExamsCountProperty() {
        return activeExamsCount;
    }

    public StringProperty lastSyncActivityProperty() {
        return lastSyncActivity;
    }

    public SyncHistoryStore getHistoryStore() {
        return historyStore;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        NotificationService.getInstance().registerStage(stage);
    }

    public void initializeForUser(User user) {
        this.currentUser = user;
        syncService.configureForUser(user);
        deviceRoleLabel.set(user.getRole().name());
        updateDatabaseSourceLabel();
        updateTopology();

        refreshConnectionState();

        startConnectionMonitor();
        syncService.startAutoSync();
        refreshAdminMetrics();

        if (probeOnline()) {
            syncAuthoritativePull(false);
        }
    }

    public void recordPendingChange(PendingChangeType type) {
        pendingQueue.enqueue(type);
    }

    public void syncNow(boolean manual) {
        if (syncInProgress.get()) {
            return;
        }
        manualSyncMode.set(manual);
        syncMode.set(manual ? "Manual" : "Auto");
        runSyncTask(manual ? SyncType.MANUAL : SyncType.AUTO, this::performFullSync);
    }

    /**
     * Server-authoritative pull only (used on connect/reconnect and when there are no pending local edits).
     */
    public void syncAuthoritativePull(boolean manual) {
        if (syncInProgress.get()) {
            return;
        }
        manualSyncMode.set(manual);
        syncMode.set(manual ? "Manual" : "Auto");
        runSyncTask(manual ? SyncType.MANUAL : SyncType.AUTO, this::performAuthoritativePullOnly);
    }

    public void syncNowWithProgress(Consumer<Boolean> onComplete) {
        syncAuthoritativePullWithProgress(onComplete);
    }

    public void syncAuthoritativePullWithProgress(Consumer<Boolean> onComplete) {
        if (syncInProgress.get()) {
            if (onComplete != null) {
                onComplete.accept(false);
            }
            return;
        }
        runSyncTask(SyncType.MANUAL, () -> {
            SyncResult result = pendingQueue.getPendingCount() > 0
                    ? performSyncWithPendingChanges(createProgressListener())
                    : performAuthoritativePullOnly();
            boolean ok = result != null && result.isSuccess();
            if (onComplete != null) {
                Platform.runLater(() -> onComplete.accept(ok));
            }
            return result != null ? result : SyncResult.fail("Sync failed");
        });
    }

    private void runSyncTask(SyncType type, SyncWorker worker) {
        Task<SyncResult> task = new Task<>() {
            @Override
            protected SyncResult call() {
                Platform.runLater(() -> {
                    syncInProgress.set(true);
                    connectionState.set(ConnectionState.SYNCING);
                    currentOperation.set("Synchronizing...");
                    syncProgress.set(0);
                });
                try {
                    return worker.run();
                } catch (Exception e) {
                    return SyncResult.fail(friendlyMessage(e));
                }
            }

            @Override
            protected void succeeded() {
                finishSyncTask(type, getValue());
            }

            @Override
            protected void failed() {
                finishSyncTask(type, SyncResult.fail(friendlyMessage(getException())));
            }
        };
        Thread t = new Thread(task, "sync-ui-task");
        t.setDaemon(true);
        t.start();
    }

    @FunctionalInterface
    private interface SyncWorker {
        SyncResult run() throws Exception;
    }

    private void finishSyncTask(SyncType type, SyncResult result) {
        Platform.runLater(() -> {
            syncInProgress.set(false);
            syncProgress.set(1);
            currentOperation.set("Idle");
            String role = deviceRoleLabel.get();
            boolean success = result != null && result.isSuccess();
            historyStore.add(type, role, success,
                    result != null ? result.getMessage() : "Unknown",
                    success ? "" : (result != null ? result.getMessage() : ""));
            lastSyncActivity.set(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            if (success) {
                lastSyncTime.set(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pendingQueue.clear();
                connectionState.set(probeOnline() ? ConnectionState.ONLINE : ConnectionState.OFFLINE);
                toast(success ? NotificationService.ToastType.SUCCESS : NotificationService.ToastType.INFO,
                        result.getMessage());
                String toastMsg;
                if (DatabaseConnection.isAdminDevice()) {
                    toastMsg = "Local backup saved successfully";
                } else if (lastSyncWasAuthoritativePull) {
                    toastMsg = MSG_SYNCED_WITH_SERVER;
                } else {
                    toastMsg = "Successfully synced from central database";
                }
                NotificationService.getInstance().showSuccess(primaryStage, toastMsg);
            } else {
                connectionState.set(probeOnline() ? ConnectionState.RECONNECTING : ConnectionState.OFFLINE);
                String msg = result != null ? result.getMessage() : "Sync failed";
                toast(NotificationService.ToastType.ERROR, msg);
                NotificationService.getInstance().showError(primaryStage, msg);
            }
            refreshAdminMetrics();
        });
    }

    private SyncProgressListener createProgressListener() {
        return (label, fraction) -> Platform.runLater(() -> {
            currentOperation.set(label);
            syncProgress.set(fraction);
        });
    }

    private SyncResult performFullSync() throws Exception {
        SyncProgressListener listener = createProgressListener();

        if (DatabaseConnection.isAdminDevice()) {
            lastSyncWasAuthoritativePull = true;
            listener.onProgress("Backing up to local H2...", 0.1);
            try (Connection central = DatabaseConnection.getCentralConnection();
                    Connection backup = DatabaseConnection.getBackupConnection()) {
                SyncBundle bundle = databaseSyncService.exportAll(central, listener);
                return databaseSyncService.importAll(backup, bundle, true, listener);
            }
        }

        if (!probeOnline()) {
            connectionState.set(ConnectionState.OFFLINE);
            NotificationService.getInstance().showWarning(primaryStage,
                    "Unable to reach central server. Continuing in offline mode.");
            return SyncResult.fail("Offline — changes saved locally");
        }

        registerClientHeartbeat();

        if (pendingQueue.getPendingCount() > 0) {
            return performSyncWithPendingChanges(listener);
        }
        return performAuthoritativePullOnly();
    }

    /**
     * Server is source of truth: replace local H2 with central data. No conflict checks.
     */
    private SyncResult performAuthoritativePullOnly() throws Exception {
        lastSyncWasAuthoritativePull = true;
        SyncProgressListener listener = createProgressListener();

        if (DatabaseConnection.isAdminDevice()) {
            return performFullSyncAdminBackup(listener);
        }

        if (!probeOnline()) {
            return SyncResult.fail("Cannot reach central server");
        }

        registerClientHeartbeat();
        listener.onProgress("Fetching data from central server...", 0.2);
        SyncResult pullResult = pullWithProgress(listener);
        if (pullResult.isSuccess()) {
            return SyncResult.ok(MSG_SYNCED_WITH_SERVER, pullResult.getTablesSynced(),
                    pullResult.getRowsSynced());
        }
        return pullResult;
    }

    private SyncResult performFullSyncAdminBackup(SyncProgressListener listener) throws Exception {
        try (Connection central = DatabaseConnection.getCentralConnection();
                Connection backup = DatabaseConnection.getBackupConnection()) {
            SyncBundle bundle = databaseSyncService.exportAll(central, listener);
            return databaseSyncService.importAll(backup, bundle, true, listener);
        }
    }

    /**
     * When the user has unsynced local edits: push first, with conflict UI only if server also changed those rows.
     */
    private SyncResult performSyncWithPendingChanges(SyncProgressListener listener) throws Exception {
        lastSyncWasAuthoritativePull = false;

        if (!probeOnline()) {
            return SyncResult.fail("Cannot reach central server");
        }

        registerClientHeartbeat();

        listener.onProgress("Checking for concurrent edits...", 0.15);
        SyncBundle remote = RMIManager.getInstance().pullSyncBundle();
        SyncBundle local;
        try (Connection backup = DatabaseConnection.getBackupConnection()) {
            local = databaseSyncService.exportAll(backup, null);
        }

        List<SyncConflict> conflicts = conflictDetector.detect(local, remote);
        if (!conflicts.isEmpty()) {
            com.examsystem.sync.ui.ConflictDialog.Resolution resolution = promptConflictResolution(
                    conflicts.get(0));
            if (resolution == null) {
                return SyncResult.fail("Sync cancelled");
            }
            if (resolution == com.examsystem.sync.ui.ConflictDialog.Resolution.KEEP_SERVER) {
                listener.onProgress("Applying server data locally...", 0.4);
                try (Connection backup = DatabaseConnection.getBackupConnection()) {
                    databaseSyncService.importAll(backup, remote, true, listener);
                }
                pendingQueue.clear();
                return SyncResult.ok(MSG_SYNCED_WITH_SERVER, remote.getTables().size(), 0);
            }
            listener.onProgress("Uploading local changes to server...", 0.5);
            SyncResult pushResult = pushWithProgress(listener);
            if (!pushResult.isSuccess()) {
                return pushResult;
            }
            listener.onProgress("Refreshing from central server...", 0.85);
            return pullWithProgress(listener);
        }

        listener.onProgress("Uploading local changes to server...", 0.4);
        SyncResult pushResult = pushWithProgress(listener);
        if (!pushResult.isSuccess()) {
            return pushResult;
        }
        listener.onProgress("Refreshing from central server...", 0.75);
        SyncResult pullResult = pullWithProgress(listener);
        if (pullResult.isSuccess()) {
            return SyncResult.ok("Successfully synced from central database",
                    pullResult.getTablesSynced(), pullResult.getRowsSynced());
        }
        return pullResult;
    }

    private com.examsystem.sync.ui.ConflictDialog.Resolution promptConflictResolution(SyncConflict conflict)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<com.examsystem.sync.ui.ConflictDialog.Resolution> choice = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                com.examsystem.sync.ui.ConflictDialog.show(primaryStage, conflict, choice::set);
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.MINUTES);
        return choice.get();
    }

    private void registerClientHeartbeat() {
        String id = Session.getInstance().getCurrentUser() != null
                ? Session.getInstance().getCurrentUser().getUsername()
                : "client";
        ClientSessionRegistry.getInstance().heartbeat(id);
    }

    private SyncResult pullWithProgress(SyncProgressListener listener) throws Exception {
        RMIManager rmi = RMIManager.getInstance();
        if (!rmi.isClientConnected() && !rmi.connectClient()) {
            return SyncResult.fail("Cannot reach central server");
        }
        SyncBundle bundle = rmi.pullSyncBundle();
        try (Connection backup = DatabaseConnection.getBackupConnection()) {
            return databaseSyncService.importAll(backup, bundle, true, listener);
        }
    }

    private SyncResult pushWithProgress(SyncProgressListener listener) throws Exception {
        RMIManager rmi = RMIManager.getInstance();
        if (!rmi.isClientConnected() && !rmi.connectClient()) {
            return SyncResult.fail("Cannot push while offline");
        }
        try (Connection backup = DatabaseConnection.getBackupConnection()) {
            SyncBundle bundle = databaseSyncService.exportAll(backup, listener);
            return rmi.pushSyncBundle(bundle);
        }
    }

    private void startConnectionMonitor() {
        if (connectionMonitor != null) {
            connectionMonitor.stop();
        }
        connectionMonitor = new ConnectionMonitor(
                connected -> Platform.runLater(() -> {
                    if (syncInProgress.get()) {
                        return;
                    }
                    if (connected) {
                        if (connectionState.get() == ConnectionState.OFFLINE
                                || connectionState.get() == ConnectionState.RECONNECTING) {
                            connectionState.set(ConnectionState.ONLINE);
                            NotificationService.getInstance().showSuccess(primaryStage,
                                    "Reconnected to central server");
                            syncAuthoritativePull(false);
                        } else {
                            connectionState.set(ConnectionState.ONLINE);
                        }
                    } else {
                        if (connectionState.get() != ConnectionState.SYNCING) {
                            connectionState.set(ConnectionState.OFFLINE);
                            NotificationService.getInstance().showWarning(primaryStage,
                                    "You are working offline. Changes will sync when connection returns.");
                        }
                    }
                    updateTopology();
                }),
                () -> Platform.runLater(() -> {
                    currentOperation.set("Reconnecting to central server...");
                    connectionState.set(ConnectionState.RECONNECTING);
                }));
        connectionMonitor.start();
    }

    public void onExamSubmitted(boolean remoteSynced) {
        recordPendingChange(PendingChangeType.EXAM_SUBMIT);
        if (remoteSynced) {
            pendingQueue.decrement(1);
            NotificationService.getInstance().showSuccess(primaryStage,
                    "Submitted and synced successfully");
        } else {
            NotificationService.getInstance().showInfo(primaryStage,
                    "Submitted locally. Waiting for synchronization.");
        }
    }

    public boolean isOnline() {
        return connectionState.get() == ConnectionState.ONLINE || connectionState.get() == ConnectionState.SYNCING;
    }

    public boolean tryRemoteSync(Runnable remoteAction) {
        if (probeOnline()) {
            try {
                remoteAction.run();
                return true;
            } catch (Exception e) {
                logger.warn("Remote sync action failed: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Re-evaluates RMI connectivity and updates the global status indicator.
     */
    public void refreshConnectionState() {
        if (syncInProgress.get()) {
            return;
        }
        Runnable update = () -> {
            ConnectionState next = probeOnline() ? ConnectionState.ONLINE : ConnectionState.OFFLINE;
            connectionState.set(next);
            updateTopology();
        };
        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }

    private boolean probeOnline() {
        if (DatabaseConnection.isAdminDevice()) {
            return RMIManager.getInstance().isServerRunning();
        }
        RMIManager rmi = RMIManager.getInstance();
        if (!rmi.isClientConnected()) {
            rmi.connectClient();
        }
        if (!rmi.isClientConnected()) {
            return false;
        }
        try {
            return "PONG".equals(rmi.ping());
        } catch (Exception e) {
            return false;
        }
    }

    private void updateDatabaseSourceLabel() {
        if (DatabaseConnection.isAdminDevice()) {
            databaseSource.set("MySQL (Central)");
        } else {
            databaseSource.set("H2 (Local Backup)");
        }
    }

    private void updateTopology() {
        ConnectionState state = connectionState.get();
        String role = deviceRoleLabel.get();
        if (DatabaseConnection.isAdminDevice()) {
            topologyText.set("Admin Server — Central MySQL — " + state.getDisplayText());
        } else {
            String link = switch (state) {
                case ONLINE, SYNCING -> "Connected";
                case RECONNECTING -> "Reconnecting";
                default -> "Offline";
            };
            topologyText.set(role + " Device ─ " + link + " ─ Admin Server");
        }
    }

    public void refreshAdminMetrics() {
        if (!DatabaseConnection.isAdminDevice()) {
            return;
        }
        connectedClients.set(ClientSessionRegistry.getInstance().getConnectedCount());
        offlineClients.set(ClientSessionRegistry.getInstance().getStaleCount());
        try (Connection c = DatabaseConnection.getCentralConnection();
                var ps = c.prepareStatement("SELECT COUNT(*) FROM exams WHERE is_published = TRUE");
                var rs = ps.executeQuery()) {
            if (rs.next()) {
                activeExamsCount.set(rs.getInt(1));
            }
        } catch (Exception e) {
            logger.debug("Could not load admin metrics", e);
        }
    }

    public static String friendlyMessage(Throwable t) {
        if (t == null) {
            return "An unexpected error occurred";
        }
        String msg = t.getMessage();
        if (msg == null) {
            msg = t.getClass().getSimpleName();
        }
        if (msg.contains("ConnectException") || msg.contains("Connection refused") || msg.contains("Not bound")) {
            return "Unable to reach central server. Continuing in offline mode.";
        }
        if (msg.contains("SQLException") || msg.contains("Communications link")) {
            return "Database connection issue. Your work is saved locally.";
        }
        return msg.length() > 120 ? msg.substring(0, 117) + "..." : msg;
    }

    private void toast(NotificationService.ToastType type, String message) {
        if (primaryStage != null) {
            NotificationService.getInstance().show(primaryStage, message, type);
        }
    }

    public void shutdown() {
        if (connectionMonitor != null) {
            connectionMonitor.stop();
        }
        syncService.shutdown();
    }

    public void runInitialSync(Consumer<Boolean> onComplete) {
        syncAuthoritativePullWithProgress(onComplete);
    }
}
