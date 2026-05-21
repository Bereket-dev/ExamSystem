package com.examsystem.model;

import java.time.LocalDateTime;

/**
 * Submitted exam result row for teacher reports.
 */
public class ExamReportEntry {
    private String studentName;
    private String examName;
    private int marksObtained;
    private int totalMarks;
    private String submissionStatus;
    private LocalDateTime endTime;

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

    public int getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(int marksObtained) {
        this.marksObtained = marksObtained;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getPercentage() {
        if (totalMarks <= 0) {
            return 0;
        }
        return (marksObtained * 100.0) / totalMarks;
    }
}
