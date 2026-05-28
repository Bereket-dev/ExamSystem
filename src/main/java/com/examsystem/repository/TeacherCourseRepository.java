package com.examsystem.repository;

import com.examsystem.model.TeacherCourse;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TeacherCourse entity.
 * Defines operations for teacher-course assignments.
 */
public interface TeacherCourseRepository {

    /**
     * Assign a teacher to a course
     */
    void save(TeacherCourse teacherCourse);

    /**
     * Find assignment by ID
     */
    Optional<TeacherCourse> findById(int teacherCourseId);

    /**
     * Get all courses assigned to a teacher
     */
    List<TeacherCourse> findCoursesByTeacher(int teacherId);

    /**
     * Get all teachers assigned to a course
     */
    List<TeacherCourse> findTeachersByCourse(int courseId);

    /**
     * Get all active teacher-course assignments
     */
    List<TeacherCourse> findAllActive();

    /**
     * Remove teacher from course
     */
    void delete(int teacherCourseId);

    /**
     * Remove teacher from specific course
     */
    void removeTeacherFromCourse(int teacherId, int courseId);

    /**
     * Check if teacher is assigned to course
     */
    boolean isTeacherAssignedToCourse(int teacherId, int courseId);

    /**
     * Get total assignment count
     */
    long count();
}
