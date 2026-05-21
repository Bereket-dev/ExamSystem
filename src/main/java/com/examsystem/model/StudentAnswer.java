package com.examsystem.model;

/**
 * Student answer record for an exam question.
 */
public class StudentAnswer {
    private int answerId;
    private int attemptId;
    private int questionId;
    private Integer selectedOptionId;
    private String shortAnswerText;
    private Boolean isCorrect;
    private int marksObtained;

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getShortAnswerText() {
        return shortAnswerText;
    }

    public void setShortAnswerText(String shortAnswerText) {
        this.shortAnswerText = shortAnswerText;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public int getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(int marksObtained) {
        this.marksObtained = marksObtained;
    }
}
