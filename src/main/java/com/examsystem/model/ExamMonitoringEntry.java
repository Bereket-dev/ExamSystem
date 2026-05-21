package com.examsystem.model;

import java.time.LocalDateTime;

/**
 * Live exam monitoring row for teacher dashboard.
 */
public class ExamMonitoringEntry {
    private int attemptId;
    private String studentName;
    private String examName;
    private String submissionStatus;
    private LocalDateTime startTime;

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
