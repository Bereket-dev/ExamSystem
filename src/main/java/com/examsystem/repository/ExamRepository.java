package com.examsystem.repository;

import com.examsystem.model.Exam;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Exam entity.
 * Defines CRUD operations for exam management.
 */
public interface ExamRepository {

    /**
     * Save a new exam
     */
    void save(Exam exam);

    /**
     * Find exam by ID
     */
    Optional<Exam> findById(int examId);

    /**
     * Get all exams
     */
    List<Exam> findAll();

    /**
     * Get exams by teacher ID
     */
    List<Exam> findByTeacherId(int teacherId);

    /**
     * Get published exams
     */
    List<Exam> findPublished();

    /**
     * Update exam
     */
    void update(Exam exam);

    /**
     * Delete exam by ID
     */
    void delete(int examId);

    /**
     * Publish/unpublish exam
     */
    void setPublished(int examId, boolean published);

    /**
     * Get total exam count
     */
    long count();
}
