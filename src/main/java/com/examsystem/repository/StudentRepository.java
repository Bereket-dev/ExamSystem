package com.examsystem.repository;

import com.examsystem.model.Exam;
import com.examsystem.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Optional<Student> findByUserId(int userId);

    Optional<Integer> findStudentIdByUserId(int userId);

    List<Exam> findAssignedExams(int studentId);

    Optional<Integer> findAssignmentId(int studentId, int examId);

    void markAssignmentAttempted(int assignmentId);
}
