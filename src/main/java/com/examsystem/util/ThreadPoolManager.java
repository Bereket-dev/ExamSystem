package com.examsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central thread pool management for background and client handler tasks.
 * See context/PROJECT_ROADMAP.md Phase 8 and THREADING_REFERENCE.md.
 */
public class ThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    private static ThreadPoolManager instance;

    private final ExecutorService backgroundExecutor;
    private final ExecutorService clientExecutor;

    private ThreadPoolManager() {
        int backgroundSize = ConfigManager.getIntProperty("thread.pool.background.size", 4);
        int clientSize = ConfigManager.getIntProperty("thread.pool.client.size", 100);
        backgroundExecutor = Executors.newFixedThreadPool(backgroundSize, namedFactory("BackgroundWorker", true));
        clientExecutor = Executors.newFixedThreadPool(clientSize, namedFactory("TCP-ClientHandler", false));
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    public void submitBackground(Runnable task) {
        backgroundExecutor.submit(task);
    }

    public ExecutorService getClientExecutor() {
        return clientExecutor;
    }

    public synchronized void shutdown() {
        logger.info("Shutting down thread pools");
        backgroundExecutor.shutdownNow();
        clientExecutor.shutdownNow();
    }

    private static ThreadFactory namedFactory(String prefix, boolean daemon) {
        AtomicInteger counter = new AtomicInteger(0);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(daemon);
            return thread;
        };
    }
}
