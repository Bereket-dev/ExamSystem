package com.examsystem.service;

import com.examsystem.model.Exam;
import com.examsystem.repository.ExamRepository;
import com.examsystem.repository.ExamRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for exam operations
 */
public class ExamService {
    private final ExamRepository examRepository;

    public ExamService() {
        this.examRepository = new ExamRepositoryImpl();
    }

    public void saveExam(Exam exam) {
        examRepository.save(exam);
    }

    public Optional<Exam> getExamById(int id) {
        return examRepository.findById(id);
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public List<Exam> getPublishedExams() {
        return examRepository.findPublished();
    }

    public void publishExam(int examId, boolean publish) {
        examRepository.setPublished(examId, publish);
    }
}
