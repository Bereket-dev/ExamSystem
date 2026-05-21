package com.examsystem.repository;

import com.examsystem.model.StudentAnswer;

import java.util.List;
import java.util.Optional;

public interface StudentAnswerRepository {
    Optional<StudentAnswer> findByAttemptAndQuestion(int attemptId, int questionId);

    void saveOrUpdate(StudentAnswer answer);

    List<StudentAnswer> findByAttemptId(int attemptId);
}
