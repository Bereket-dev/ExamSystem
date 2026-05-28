package com.examsystem.sync;

/**
 * A teacher/student device that registered with the admin server over RMI.
 */
public record ClientConnectionEvent(
        String username,
        String fullName,
        String role,
        String message,
        long connectedAtMs) {
}
