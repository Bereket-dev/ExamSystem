package com.examsystem.sync;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks connected client devices for admin dashboard metrics.
 */
public final class ClientSessionRegistry {
    private static final ClientSessionRegistry INSTANCE = new ClientSessionRegistry();
    private final Map<String, LocalDateTime> activeClients = new ConcurrentHashMap<>();

    private ClientSessionRegistry() {
    }

    public static ClientSessionRegistry getInstance() {
        return INSTANCE;
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
