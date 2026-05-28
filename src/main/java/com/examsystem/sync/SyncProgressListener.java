package com.examsystem.sync;

@FunctionalInterface
public interface SyncProgressListener {
    void onProgress(String stepLabel, double progressFraction);
}
