package com.examsystem.sync;

public enum PendingChangeType {
    EXAM_EDIT("Exam change"),
    ANSWER_SAVE("Answer saved"),
    EXAM_SUBMIT("Exam submitted"),
    ASSIGNMENT("Student assignment"),
    USER_UPDATE("User update"),
    OTHER("Local change");

    private final String label;

    PendingChangeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
