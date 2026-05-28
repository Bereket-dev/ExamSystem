package com.examsystem.service;

import com.examsystem.model.AssignedExam;
import com.examsystem.model.Exam;
import com.examsystem.model.ExamAttempt;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import com.examsystem.model.Student;
import com.examsystem.model.StudentAnswer;
import com.examsystem.repository.ExamAttemptRepository;
import com.examsystem.repository.ExamAttemptRepositoryImpl;
import com.examsystem.repository.ExamRepository;
import com.examsystem.repository.ExamRepositoryImpl;
import com.examsystem.repository.QuestionRepository;
import com.examsystem.repository.QuestionRepositoryImpl;
import com.examsystem.repository.StudentAnswerRepository;
import com.examsystem.repository.StudentAnswerRepositoryImpl;
import com.examsystem.repository.StudentRepository;
import com.examsystem.repository.StudentRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final StudentAnswerRepository answerRepository;

    public StudentService() {
        this.studentRepository = new StudentRepositoryImpl();
        this.examRepository = new ExamRepositoryImpl();
        this.questionRepository = new QuestionRepositoryImpl();
        this.attemptRepository = new ExamAttemptRepositoryImpl();
        this.answerRepository = new StudentAnswerRepositoryImpl();
    }

    public Optional<Student> findByUserId(int userId) {
        return studentRepository.findByUserId(userId);
    }

    public Optional<Integer> findStudentIdByUserId(int userId) {
        return studentRepository.findStudentIdByUserId(userId);
    }

    public List<Exam> getAssignedExams(int studentId) {
        return studentRepository.findAssignedExams(studentId);
    }

    public List<AssignedExam> getAssignedExamsWithStatus(int studentId) {
        return studentRepository.findAssignedExamsWithStatus(studentId);
    }

    public Optional<Integer> findAssignmentId(int studentId, int examId) {
        return studentRepository.findAssignmentId(studentId, examId);
    }

    public boolean isAssignmentAttempted(int assignmentId) {
        return studentRepository.isAssignmentAttempted(assignmentId);
    }

    public void markAssignmentAttempted(int assignmentId) {
        studentRepository.markAssignmentAttempted(assignmentId);
    }

    public ExamAttempt createOrRetrieveAttempt(int assignmentId) {
        return attemptRepository.findByAssignmentId(assignmentId)
                .orElseGet(() -> attemptRepository.createAttempt(assignmentId));
    }

    public Optional<ExamAttempt> getAttemptByAssignment(int assignmentId) {
        return attemptRepository.findByAssignmentId(assignmentId);
    }

    public void updateAttempt(ExamAttempt attempt) {
        attemptRepository.updateAttempt(attempt);
    }

    public List<Question> getQuestions(int examId) {
        return questionRepository.findByExamId(examId);
    }

    public List<Question> getRandomizedQuestions(int examId) {
        List<Question> questions = new java.util.ArrayList<>(questionRepository.findByExamId(examId));
        java.util.Collections.shuffle(questions);
        return questions;
    }

    public List<QuestionOption> getOptions(int questionId) {
        return questionRepository.findOptionsByQuestionId(questionId);
    }

    public List<QuestionOption> getRandomizedOptions(int questionId) {
        List<QuestionOption> options = new java.util.ArrayList<>(questionRepository.findOptionsByQuestionId(questionId));
        java.util.Collections.shuffle(options);
        return options;
    }

    public Optional<StudentAnswer> getAnswer(int attemptId, int questionId) {
        return answerRepository.findByAttemptAndQuestion(attemptId, questionId);
    }

    public void saveStudentAnswer(StudentAnswer answer) {
        answerRepository.saveOrUpdate(answer);
    }

    public List<StudentAnswer> getAnswersByAttempt(int attemptId) {
        return answerRepository.findByAttemptId(attemptId);
    }
}
