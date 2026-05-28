package com.examsystem.service;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.User;
import com.examsystem.model.Teacher;
import com.examsystem.model.Student;
import com.examsystem.model.Course;
import com.examsystem.model.TeacherCourse;
import com.examsystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * AdminService provides business logic for admin operations.
 * Handles user, teacher, student, course, and teacher-course management.
 */
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherCourseRepository teacherCourseRepository;

    public AdminService() {
        this.userRepository = new UserRepositoryImpl();
        this.teacherRepository = new TeacherRepositoryImpl();
        this.studentRepository = new StudentRepositoryImpl();
        this.courseRepository = new CourseRepositoryImpl();
        this.teacherCourseRepository = new TeacherCourseRepositoryImpl();
    }

    // ============ TEACHER MANAGEMENT ============

    /**
     * Add a new teacher account
     */
    public void addTeacher(User user, Teacher teacher) {
        try {
            // Validate input
            if (user == null || teacher == null) {
                throw new IllegalArgumentException("User and Teacher information cannot be null");
            }

            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }

            // Set role to TEACHER
            user.setRole(User.UserRole.TEACHER);

            // Save user first
            userRepository.save(user);

            // Set user ID and save teacher
            teacher.setUserId(user.getUserId());
            saveTeacherProfile(teacher);

            logger.info("Teacher added successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error adding teacher", e);
            throw e;
        }
    }

    /**
     * Update teacher information
     */
    public void updateTeacher(User user, Teacher teacher) {
        try {
            if (user == null || teacher == null) {
                throw new IllegalArgumentException("User and Teacher information cannot be null");
            }

            userRepository.update(user);
            updateTeacherProfile(teacher);

            logger.info("Teacher updated successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error updating teacher", e);
            throw e;
        }
    }

    /**
     * Remove a teacher account
     */
    public void removeTeacher(int userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent() && user.get().getRole() == User.UserRole.TEACHER) {
                // Remove teacher profile
                Optional<Teacher> teacher = teacherRepository.findByUserId(userId);
                if (teacher.isPresent()) {
                    deleteTeacherProfile(teacher.get().getTeacherId());
                }
                // Remove user account
                userRepository.delete(userId);
                logger.info("Teacher removed successfully with user ID: {}", userId);
            } else {
                throw new IllegalArgumentException("User is not a teacher");
            }
        } catch (Exception e) {
            logger.error("Error removing teacher", e);
            throw e;
        }
    }

    /**
     * Get all teachers
     */
    public List<User> getAllTeachers() {
        return userRepository.findByRole(User.UserRole.TEACHER.toString().toLowerCase());
    }

    /**
     * Get teacher details by user ID
     */
    public Optional<Teacher> getTeacherByUserId(int userId) {
        return teacherRepository.findByUserId(userId);
    }

    // ============ STUDENT MANAGEMENT ============

    /**
     * Add a new student account
     */
    public void addStudent(User user, Student student) {
        try {
            // Validate input
            if (user == null || student == null) {
                throw new IllegalArgumentException("User and Student information cannot be null");
            }

            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }

            // Set role to STUDENT
            user.setRole(User.UserRole.STUDENT);

            // Save user first
            userRepository.save(user);

            // Set user ID and save student
            student.setUserId(user.getUserId());
            saveStudentProfile(student);

            logger.info("Student added successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error adding student", e);
            throw e;
        }
    }

    /**
     * Update student information
     */
    public void updateStudent(User user, Student student) {
        try {
            if (user == null || student == null) {
                throw new IllegalArgumentException("User and Student information cannot be null");
            }

            userRepository.update(user);
            updateStudentProfile(student);

            logger.info("Student updated successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error updating student", e);
            throw e;
        }
    }

    /**
     * Remove a student account
     */
    public void removeStudent(int userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent() && user.get().getRole() == User.UserRole.STUDENT) {
                // Remove student profile
                Optional<Student> student = studentRepository.findByUserId(userId);
                if (student.isPresent()) {
                    deleteStudentProfile(student.get().getStudentId());
                }
                // Remove user account
                userRepository.delete(userId);
                logger.info("Student removed successfully with user ID: {}", userId);
            } else {
                throw new IllegalArgumentException("User is not a student");
            }
        } catch (Exception e) {
            logger.error("Error removing student", e);
            throw e;
        }
    }

    /**
     * Get all students
     */
    public List<User> getAllStudents() {
        return userRepository.findByRole(User.UserRole.STUDENT.toString().toLowerCase());
    }

    /**
     * Get student details by user ID
     */
    public Optional<Student> getStudentByUserId(int userId) {
        return studentRepository.findByUserId(userId);
    }

    // ============ COURSE MANAGEMENT ============

    /**
     * Create a new course
     */
    public void createCourse(Course course) {
        try {
            if (course == null) {
                throw new IllegalArgumentException("Course information cannot be null");
            }

            if (courseRepository.findByCourseCode(course.getCourseCode()).isPresent()) {
                throw new IllegalArgumentException("Course code already exists: " + course.getCourseCode());
            }

            courseRepository.save(course);
            logger.info("Course created successfully: {}", course.getCourseCode());
        } catch (Exception e) {
            logger.error("Error creating course", e);
            throw e;
        }
    }

    /**
     * Update course information
     */
    public void updateCourse(Course course) {
        try {
            if (course == null) {
                throw new IllegalArgumentException("Course information cannot be null");
            }

            courseRepository.update(course);
            logger.info("Course updated successfully: {}", course.getCourseCode());
        } catch (Exception e) {
            logger.error("Error updating course", e);
            throw e;
        }
    }

    /**
     * Delete a course
     */
    public void deleteCourse(int courseId) {
        try {
            // First, remove all teacher assignments for this course
            List<TeacherCourse> assignments = teacherCourseRepository.findTeachersByCourse(courseId);
            for (TeacherCourse assignment : assignments) {
                teacherCourseRepository.delete(assignment.getTeacherCourseId());
            }

            // Then delete the course
            courseRepository.delete(courseId);
            logger.info("Course deleted successfully with ID: {}", courseId);
        } catch (Exception e) {
            logger.error("Error deleting course", e);
            throw e;
        }
    }

    /**
     * Get all courses
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get all active courses
     */
    public List<Course> getAllActiveCourses() {
        return courseRepository.findAllActive();
    }

    /**
     * Get course by ID
     */
    public Optional<Course> getCourseById(int courseId) {
        return courseRepository.findById(courseId);
    }

    /**
     * Get courses by department
     */
    public List<Course> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartment(department);
    }

    /**
     * Get courses by semester
     */
    public List<Course> getCoursesBySemester(int semester) {
        return courseRepository.findBySemester(semester);
    }

    // ============ TEACHER-COURSE MANAGEMENT ============

    /**
     * Assign a teacher to a course
     */
    public void assignTeacherToCourse(int teacherId, int courseId) {
        try {
            // Verify teacher and course exist
            boolean teacherExists = existsTeacherByTeacherId(teacherId);
            Optional<Course> course = courseRepository.findById(courseId);

            if (!teacherExists) {
                throw new IllegalArgumentException("Teacher not found with ID: " + teacherId);
            }
            if (course.isEmpty()) {
                throw new IllegalArgumentException("Course not found with ID: " + courseId);
            }

            // Check if already assigned
            if (teacherCourseRepository.isTeacherAssignedToCourse(teacherId, courseId)) {
                throw new IllegalArgumentException("Teacher is already assigned to this course");
            }

            // Create assignment
            TeacherCourse assignment = new TeacherCourse(teacherId, courseId);
            teacherCourseRepository.save(assignment);

            logger.info("Teacher {} assigned to course {} successfully", teacherId, courseId);
        } catch (Exception e) {
            logger.error("Error assigning teacher to course", e);
            throw e;
        }
    }

    /**
     * Remove teacher from a course
     */
    public void removeTeacherFromCourse(int teacherId, int courseId) {
        try {
            // Verify assignment exists
            if (!teacherCourseRepository.isTeacherAssignedToCourse(teacherId, courseId)) {
                throw new IllegalArgumentException("Teacher is not assigned to this course");
            }

            teacherCourseRepository.removeTeacherFromCourse(teacherId, courseId);
            logger.info("Teacher {} removed from course {} successfully", teacherId, courseId);
        } catch (Exception e) {
            logger.error("Error removing teacher from course", e);
            throw e;
        }
    }

    /**
     * Get courses assigned to a teacher
     */
    public List<TeacherCourse> getCoursesForTeacher(int teacherId) {
        return teacherCourseRepository.findCoursesByTeacher(teacherId);
    }

    /**
     * Get teachers assigned to a course
     */
    public List<TeacherCourse> getTeachersForCourse(int courseId) {
        return teacherCourseRepository.findTeachersByCourse(courseId);
    }

    /**
     * Check if teacher is assigned to course
     */
    public boolean isTeacherAssignedToCourse(int teacherId, int courseId) {
        return teacherCourseRepository.isTeacherAssignedToCourse(teacherId, courseId);
    }

    private void saveTeacherProfile(Teacher teacher) {
        String sql = "INSERT INTO teachers (user_id, department, qualification, experience_years) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacher.getUserId());
            stmt.setString(2, teacher.getDepartment());
            stmt.setString(3, teacher.getQualification());
            stmt.setInt(4, teacher.getExperienceYears());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save teacher profile", e);
        }
    }

    private void updateTeacherProfile(Teacher teacher) {
        String sql = "UPDATE teachers SET department = ?, qualification = ?, experience_years = ? WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacher.getDepartment());
            stmt.setString(2, teacher.getQualification());
            stmt.setInt(3, teacher.getExperienceYears());
            stmt.setInt(4, teacher.getTeacherId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update teacher profile", e);
        }
    }

    private void deleteTeacherProfile(int teacherId) {
        String sql = "DELETE FROM teachers WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete teacher profile", e);
        }
    }

    private void saveStudentProfile(Student student) {
        String sql = "INSERT INTO students (user_id, enrollment_number, department, semester) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getEnrollmentNumber());
            stmt.setString(3, student.getDepartment());
            stmt.setInt(4, student.getSemester());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save student profile", e);
        }
    }

    private void updateStudentProfile(Student student) {
        String sql = "UPDATE students SET enrollment_number = ?, department = ?, semester = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getEnrollmentNumber());
            stmt.setString(2, student.getDepartment());
            stmt.setInt(3, student.getSemester());
            stmt.setInt(4, student.getStudentId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student profile", e);
        }
    }

    private void deleteStudentProfile(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete student profile", e);
        }
    }

    private boolean existsTeacherByTeacherId(int teacherId) {
        String sql = "SELECT 1 FROM teachers WHERE teacher_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify teacher existence", e);
        }
    }
}
