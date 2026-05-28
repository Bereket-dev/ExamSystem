package com.examsystem.repository;

import com.examsystem.model.AssignedExam;
import com.examsystem.model.Exam;
import com.examsystem.model.ExamAssignmentSyncResult;
import com.examsystem.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Optional<Student> findByUserId(int userId);

    Optional<Integer> findStudentIdByUserId(int userId);

    List<Exam> findAssignedExams(int studentId);

    List<AssignedExam> findAssignedExamsWithStatus(int studentId);

    List<Student> findAll();

    List<Student> findByDepartmentAndSemester(String department, int semester);

    Optional<Integer> findAssignmentId(int studentId, int examId);

    boolean isAssignmentAttempted(int assignmentId);

    void markAssignmentAttempted(int assignmentId);

    void assignExamToStudent(int examId, int studentId);

    /**
     * Assigns multiple students to an exam. Returns the number of new assignments created.
     */
    int assignExamToStudents(int examId, List<Integer> studentIds);

    /**
     * Synchronizes assignments for an exam with the given selected student IDs.
     * Checked students are assigned; unchecked students are removed from the exam.
     */
    ExamAssignmentSyncResult syncExamAssignments(int examId, List<Integer> selectedStudentIds);
}
