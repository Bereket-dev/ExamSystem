package com.examsystem.rmi.remote;

import java.io.Serializable;

/**
 * Serializable answer payload for RMI save operations.
 */
public class RemoteAnswerPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private int attemptId;
    private int questionId;
    private Integer selectedOptionId;
    private String shortAnswerText;
    private Boolean correct;
    private int marksObtained;

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

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public int getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(int marksObtained) {
        this.marksObtained = marksObtained;
    }
}
