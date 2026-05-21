package com.examsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * Daemon countdown timer thread for exam duration.
 * See context/THREADING_REFERENCE.md - Timer Thread.
 */
public class ExamTimerThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ExamTimerThread.class);

    private final AtomicInteger remainingSeconds;
    private final IntConsumer onTick;
    private final Runnable onExpire;
    private volatile boolean running = true;

    public ExamTimerThread(int initialSeconds, IntConsumer onTick, Runnable onExpire) {
        super("TimerThread");
        this.remainingSeconds = new AtomicInteger(Math.max(0, initialSeconds));
        this.onTick = onTick;
        this.onExpire = onExpire;
        setDaemon(true);
    }

    @Override
    public void run() {
        logger.info("Exam timer started with {} seconds", remainingSeconds.get());
        while (running) {
            int current = remainingSeconds.get();
            onTick.accept(current);
            if (current <= 0) {
                onExpire.run();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            remainingSeconds.decrementAndGet();
        }
        logger.info("Exam timer stopped");
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }
}
