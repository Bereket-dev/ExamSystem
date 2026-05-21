package com.examsystem.model;

import java.time.LocalDateTime;

/**
 * Stores a student exam attempt record.
 */
public class ExamAttempt {
    private int attemptId;
    private int assignmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalMarksObtained;
    private String submissionStatus;

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getTotalMarksObtained() {
        return totalMarksObtained;
    }

    public void setTotalMarksObtained(int totalMarksObtained) {
        this.totalMarksObtained = totalMarksObtained;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }
}
