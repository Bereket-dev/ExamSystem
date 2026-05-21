package com.examsystem.model;

/**
 * Question Model class representing an exam question.
 */
public class Question {
    private int questionId;
    private int examId;
    private String questionText;
    private QuestionType questionType;
    private int marks;
    private int sequenceOrder;
    private DifficultyLevel difficultyLevel;

    public enum QuestionType {
        MCQ, TRUE_FALSE, SHORT_ANSWER
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    // Constructors
    public Question() {
    }

    public Question(int examId, String questionText, QuestionType questionType, int marks, int sequenceOrder) {
        this.examId = examId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks;
        this.sequenceOrder = sequenceOrder;
        this.difficultyLevel = DifficultyLevel.MEDIUM;
    }

    // Getters and Setters
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", examId=" + examId +
                ", questionType=" + questionType +
                ", marks=" + marks +
                ", difficultyLevel=" + difficultyLevel +
                '}';
    }
}
