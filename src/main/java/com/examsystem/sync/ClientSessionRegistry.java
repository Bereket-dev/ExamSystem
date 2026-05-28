package com.examsystem.sync;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks connected client devices for admin dashboard metrics and setup-screen handshakes.
 */
public final class ClientSessionRegistry {
    private static final ClientSessionRegistry INSTANCE = new ClientSessionRegistry();
    private final Map<String, LocalDateTime> activeClients = new ConcurrentHashMap<>();
    private final AtomicReference<ClientConnectionEvent> latestConnection = new AtomicReference<>();
    private volatile long waitingSinceMs;

    private ClientSessionRegistry() {
    }

    public static ClientSessionRegistry getInstance() {
        return INSTANCE;
    }

    /** Admin setup: reset and wait for the next client registration. */
    public void beginWaitingForClient() {
        waitingSinceMs = System.currentTimeMillis();
        latestConnection.set(null);
    }

    public boolean isWaitingForClient() {
        return waitingSinceMs > 0;
    }

    public void cancelWaitingForClient() {
        waitingSinceMs = 0;
        latestConnection.set(null);
    }

    /**
     * Called on the admin server when a teacher/student completes online setup.
     */
    public ClientConnectionEvent registerClientConnection(String username, String fullName, String role) {
        String displayRole = role == null ? "client" : role.toLowerCase();
        String message = fullName + " (" + displayRole + ") connected successfully";
        ClientConnectionEvent event = new ClientConnectionEvent(
                username, fullName, role, message, System.currentTimeMillis());
        latestConnection.set(event);
        heartbeat(username);
        return event;
    }

    /**
     * Returns a new client event received after {@link #beginWaitingForClient()}, once.
     */
    public Optional<ClientConnectionEvent> pollConnectionWhileWaiting() {
        ClientConnectionEvent event = latestConnection.get();
        if (event == null || event.connectedAtMs() < waitingSinceMs) {
            return Optional.empty();
        }
        latestConnection.set(null);
        waitingSinceMs = 0;
        return Optional.of(event);
    }

    public void heartbeat(String clientId) {
        activeClients.put(clientId, LocalDateTime.now());
    }

    public void remove(String clientId) {
        activeClients.remove(clientId);
    }

    public int getConnectedCount() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        activeClients.entrySet().removeIf(e -> e.getValue().isBefore(cutoff));
        return activeClients.size();
    }

    public int getStaleCount() {
        return activeClients.size() - getConnectedCount();
    }
}
