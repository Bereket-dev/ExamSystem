package com.examsystem.connection;

/**
 * Saved distributed connectivity preferences (local per device).
 */
public class ConnectionProfile {
    private String serverHost = "localhost";
    private int rmiPort = 1099;
    private boolean offlineMode;
    private boolean setupCompleted;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isSetupCompleted() {
        return setupCompleted;
    }

    public void setSetupCompleted(boolean setupCompleted) {
        this.setupCompleted = setupCompleted;
    }

    public boolean isValidForAutoConnect() {
        return setupCompleted && serverHost != null && !serverHost.isBlank() && rmiPort > 0 && rmiPort <= 65535;
    }
}
