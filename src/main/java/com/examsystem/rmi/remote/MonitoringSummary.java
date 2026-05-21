package com.examsystem.rmi.remote;

import java.io.Serializable;

/**
 * Serializable monitoring summary for teacher RMI clients.
 */
public class MonitoringSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int activeAttemptCount;
    private int submittedReportCount;

    public MonitoringSummary(int activeAttemptCount, int submittedReportCount) {
        this.activeAttemptCount = activeAttemptCount;
        this.submittedReportCount = submittedReportCount;
    }

    public int getActiveAttemptCount() {
        return activeAttemptCount;
    }

    public int getSubmittedReportCount() {
        return submittedReportCount;
    }
}
