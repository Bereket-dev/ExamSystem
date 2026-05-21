package com.examsystem.util;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Loads data on a background thread and applies UI updates on the JavaFX thread.
 * See context/THREADING_REFERENCE.md and KNOWN_ISSUES.md (JavaFX freeze fix).
 */
public final class BackgroundLoader {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundLoader.class);

    private BackgroundLoader() {
    }

    public static <T> void load(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {
        ThreadPoolManager.getInstance().submitBackground(() -> {
            try {
                T result = backgroundTask.get();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception e) {
                logger.error("Background load failed", e);
                Platform.runLater(() -> onError.accept(e));
            }
        });
    }

    public static void run(Runnable backgroundTask, Runnable onSuccess, Consumer<Throwable> onError) {
        load(() -> {
            backgroundTask.run();
            return null;
        }, ignored -> onSuccess.run(), onError);
    }
}
