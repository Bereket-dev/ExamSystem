package com.examsystem.model;

/**
 * Result of synchronizing exam-student assignments with the database.
 */
public class ExamAssignmentSyncResult {
    private final int newlyAssigned;
    private final int unassigned;

    public ExamAssignmentSyncResult(int newlyAssigned, int unassigned) {
        this.newlyAssigned = newlyAssigned;
        this.unassigned = unassigned;
    }

    public int getNewlyAssigned() {
        return newlyAssigned;
    }

    public int getUnassigned() {
        return unassigned;
    }

    public boolean hasChanges() {
        return newlyAssigned > 0 || unassigned > 0;
    }
}
