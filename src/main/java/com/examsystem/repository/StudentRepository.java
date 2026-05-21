package com.examsystem.repository;

import com.examsystem.model.AssignedExam;
import com.examsystem.model.Exam;
import com.examsystem.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Optional<Student> findByUserId(int userId);

    Optional<Integer> findStudentIdByUserId(int userId);

    List<Exam> findAssignedExams(int studentId);

    List<AssignedExam> findAssignedExamsWithStatus(int studentId);

    List<Student> findAll();

    Optional<Integer> findAssignmentId(int studentId, int examId);

    boolean isAssignmentAttempted(int assignmentId);

    void markAssignmentAttempted(int assignmentId);

    void assignExamToStudent(int examId, int studentId);
}
