package com.examsystem.model;

/**
 * Exam assigned to a student with assignment metadata.
 */
public class AssignedExam {
    private Exam exam;
    private int assignmentId;
    private boolean attempted;

    public AssignedExam(Exam exam, int assignmentId, boolean attempted) {
        this.exam = exam;
        this.assignmentId = assignmentId;
        this.attempted = attempted;
    }

    public Exam getExam() {
        return exam;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public boolean isAttempted() {
        return attempted;
    }
}
