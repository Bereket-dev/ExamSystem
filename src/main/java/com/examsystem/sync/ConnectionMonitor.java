package com.examsystem.sync;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.rmi.RMIManager;
import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Periodically checks RMI connectivity and drives reconnection + auto-sync.
 */
public class ConnectionMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionMonitor.class);

    private final Consumer<Boolean> onConnectionChanged;
    private final Runnable onReconnected;
    private ScheduledExecutorService executor;
    private volatile boolean lastConnected;

    public ConnectionMonitor(Consumer<Boolean> onConnectionChanged, Runnable onReconnected) {
        this.onConnectionChanged = onConnectionChanged;
        this.onReconnected = onReconnected;
    }

    public void start() {
        if (executor != null) {
            return;
        }
        int intervalSec = ConfigManager.getIntProperty("sync.reconnect.interval.seconds", 5);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "connection-monitor");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::checkConnection, 0, intervalSec, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public boolean checkConnectionNow() {
        boolean connected = probeConnection();
        handleStateChange(connected);
        return connected;
    }

    private void checkConnection() {
        boolean connected = probeConnection();
        handleStateChange(connected);
    }

    private boolean probeConnection() {
        if (DatabaseConnection.isAdminDevice()) {
            return RMIManager.getInstance().isServerRunning();
        }
        RMIManager rmi = RMIManager.getInstance();
        if (rmi.isClientConnected()) {
            try {
                return "PONG".equals(rmi.ping());
            } catch (Exception e) {
                return false;
            }
        }
        return rmi.connectClient();
    }

    private void handleStateChange(boolean connected) {
        if (connected == lastConnected) {
            return;
        }
        boolean wasOffline = !lastConnected;
        lastConnected = connected;
        onConnectionChanged.accept(connected);
        if (connected && wasOffline && onReconnected != null) {
            onReconnected.run();
        }
    }
}
