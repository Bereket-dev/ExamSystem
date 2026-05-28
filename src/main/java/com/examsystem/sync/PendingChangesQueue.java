package com.examsystem.sync;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe queue of unsynced local operations with observable pending count.
 */
public class PendingChangesQueue {
    private final Deque<PendingChangeType> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final IntegerProperty pendingCount = new SimpleIntegerProperty(0);
    private final ObservableList<String> recentDescriptions = FXCollections.observableArrayList();

    public IntegerProperty pendingCountProperty() {
        return pendingCount;
    }

    public int getPendingCount() {
        return pendingCount.get();
    }

    public ObservableList<String> getRecentDescriptions() {
        return recentDescriptions;
    }

    public void enqueue(PendingChangeType type) {
        lock.lock();
        try {
            queue.addLast(type);
            pendingCount.set(queue.size());
            javafx.application.Platform.runLater(() -> {
                recentDescriptions.add(0, type.getLabel());
                while (recentDescriptions.size() > 8) {
                    recentDescriptions.remove(recentDescriptions.size() - 1);
                }
            });
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            queue.clear();
            pendingCount.set(0);
            javafx.application.Platform.runLater(recentDescriptions::clear);
        } finally {
            lock.unlock();
        }
    }

    public void decrement(int count) {
        lock.lock();
        try {
            for (int i = 0; i < count && !queue.isEmpty(); i++) {
                queue.pollFirst();
            }
            pendingCount.set(queue.size());
        } finally {
            lock.unlock();
        }
    }
}
