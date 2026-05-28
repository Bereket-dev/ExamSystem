package com.examsystem.repository;

import com.examsystem.model.Course;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Course entity.
 * Defines CRUD operations for course management.
 */
public interface CourseRepository {

    /**
     * Save a new course to the database
     */
    void save(Course course);

    /**
     * Find course by ID
     */
    Optional<Course> findById(int courseId);

    /**
     * Find course by course code
     */
    Optional<Course> findByCourseCode(String courseCode);

    /**
     * Get all courses
     */
    List<Course> findAll();

    /**
     * Get all active courses
     */
    List<Course> findAllActive();

    /**
     * Get courses by department
     */
    List<Course> findByDepartment(String department);

    /**
     * Get courses by semester
     */
    List<Course> findBySemester(int semester);

    /**
     * Update course information
     */
    void update(Course course);

    /**
     * Delete course by ID
     */
    void delete(int courseId);

    /**
     * Get total course count
     */
    long count();
}
