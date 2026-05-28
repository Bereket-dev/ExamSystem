package com.examsystem.connection;

/**
 * Outcome of an RMI connectivity probe.
 */
public record ConnectionTestResult(boolean success, String message) {
    public static ConnectionTestResult ok() {
        return new ConnectionTestResult(true, "Connected successfully to the admin RMI server.");
    }

    public static ConnectionTestResult fail(String reason) {
        String msg = reason == null || reason.isBlank()
                ? "Could not reach the admin server. Check the IP address, port, and firewall settings."
                : reason;
        return new ConnectionTestResult(false, msg);
    }
}
