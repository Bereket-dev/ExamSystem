package com.examsystem.repository;

import com.examsystem.model.ExamAttempt;
import java.util.Optional;

public interface ExamAttemptRepository {
    Optional<ExamAttempt> findByAssignmentId(int assignmentId);

    ExamAttempt createAttempt(int assignmentId);

    void updateAttempt(ExamAttempt attempt);
}
