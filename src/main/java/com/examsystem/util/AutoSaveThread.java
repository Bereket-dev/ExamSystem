package com.examsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background daemon thread that periodically saves exam answers.
 * See context/THREADING_REFERENCE.md
 */
public class AutoSaveThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AutoSaveThread.class);
    private static final long SAVE_INTERVAL_MS = 30_000;

    private final Runnable saveTask;
    private volatile boolean running = true;

    public AutoSaveThread(Runnable saveTask) {
        super("AutoSaveThread");
        this.saveTask = saveTask;
        setDaemon(true);
    }

    @Override
    public void run() {
        logger.info("Auto-save thread started");
        while (running) {
            try {
                Thread.sleep(SAVE_INTERVAL_MS);
                if (running) {
                    saveTask.run();
                    logger.debug("Auto-save completed");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Auto-save failed", e);
            }
        }
        logger.info("Auto-save thread stopped");
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
