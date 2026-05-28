package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.AssignedExam;
import com.examsystem.model.Exam;
import com.examsystem.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepositoryImpl implements StudentRepository {
    private static final Logger logger = LoggerFactory.getLogger(StudentRepositoryImpl.class);

    private static final String SELECT_BY_USER = "SELECT * FROM students WHERE user_id = ?";
    private static final String SELECT_STUDENT_ID = "SELECT student_id FROM students WHERE user_id = ?";
    private static final String SELECT_ASSIGNED_EXAMS = "SELECT e.* FROM exams e JOIN student_exam_assignments a ON e.exam_id = a.exam_id WHERE a.student_id = ? AND e.is_published = TRUE ORDER BY e.exam_date";
    private static final String SELECT_ASSIGNED_WITH_STATUS = "SELECT e.*, a.assignment_id, a.is_attempted FROM exams e JOIN student_exam_assignments a ON e.exam_id = a.exam_id WHERE a.student_id = ? AND e.is_published = TRUE ORDER BY e.exam_date";
    private static final String SELECT_ALL_STUDENTS = "SELECT * FROM students ORDER BY student_id";
    private static final String SELECT_STUDENTS_BY_DEPARTMENT_AND_SEMESTER = "SELECT * FROM students WHERE department = ? AND semester = ? ORDER BY student_id";
    private static final String SELECT_ASSIGNMENT_ID = "SELECT assignment_id FROM student_exam_assignments WHERE student_id = ? AND exam_id = ? LIMIT 1";
    private static final String SELECT_ASSIGNMENT_ATTEMPTED = "SELECT is_attempted FROM student_exam_assignments WHERE assignment_id = ?";
    private static final String INSERT_ASSIGNMENT = "INSERT INTO student_exam_assignments (exam_id, student_id, is_attempted) VALUES (?, ?, FALSE)";
    private static final String UPDATE_ASSIGNMENT_ATTEMPTED = "UPDATE student_exam_assignments SET is_attempted = TRUE WHERE assignment_id = ?";

    @Override
    public Optional<Student> findByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setUserId(rs.getInt("user_id"));
                    student.setEnrollmentNumber(rs.getString("enrollment_number"));
                    student.setDepartment(rs.getString("department"));
                    student.setSemester(rs.getInt("semester"));
                    return Optional.of(student);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student by user id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> findStudentIdByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_STUDENT_ID)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("student_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student id by user id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Exam> findAssignedExams(int studentId) {
        List<Exam> exams = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ASSIGNED_EXAMS)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    exams.add(mapResultSetToExam(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding assigned exams", e);
        }
        return exams;
    }

    @Override
    public List<AssignedExam> findAssignedExamsWithStatus(int studentId) {
        List<AssignedExam> assigned = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ASSIGNED_WITH_STATUS)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exam exam = mapResultSetToExam(rs);
                    int assignmentId = rs.getInt("assignment_id");
                    boolean attempted = rs.getBoolean("is_attempted");
                    assigned.add(new AssignedExam(exam, assignmentId, attempted));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding assigned exams with status", e);
        }
        return assigned;
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL_STUDENTS)) {
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setUserId(rs.getInt("user_id"));
                student.setEnrollmentNumber(rs.getString("enrollment_number"));
                student.setDepartment(rs.getString("department"));
                student.setSemester(rs.getInt("semester"));
                students.add(student);
            }
        } catch (SQLException e) {
            logger.error("Error finding all students", e);
        }
        return students;
    }

    @Override
    public List<Student> findByDepartmentAndSemester(String department, int semester) {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_STUDENTS_BY_DEPARTMENT_AND_SEMESTER)) {
            stmt.setString(1, department);
            stmt.setInt(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setUserId(rs.getInt("user_id"));
                    student.setEnrollmentNumber(rs.getString("enrollment_number"));
                    student.setDepartment(rs.getString("department"));
                    student.setSemester(rs.getInt("semester"));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding students by department and semester", e);
        }
        return students;
    }

    @Override
    public Optional<Integer> findAssignmentId(int studentId, int examId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ASSIGNMENT_ID)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("assignment_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding assignment id", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean isAssignmentAttempted(int assignmentId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ASSIGNMENT_ATTEMPTED)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_attempted");
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking assignment attempted status", e);
        }
        return false;
    }

    @Override
    public void assignExamToStudent(int examId, int studentId) {
        if (findAssignmentId(studentId, examId).isPresent()) {
            return; // already assigned, ignore duplicate assignment
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_ASSIGNMENT)) {
            stmt.setInt(1, examId);
            stmt.setInt(2, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error assigning exam to student", e);
            throw new RuntimeException("Failed to assign exam", e);
        }
    }

    @Override
    public void markAssignmentAttempted(int assignmentId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_ASSIGNMENT_ATTEMPTED)) {
            stmt.setInt(1, assignmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking assignment as attempted", e);
        }
    }

    private Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setTeacherId(rs.getInt("teacher_id"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setDescription(rs.getString("description"));
        exam.setSubject(rs.getString("subject"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setTotalQuestions(rs.getInt("total_questions"));
        exam.setTotalMarks(rs.getInt("total_marks"));
        exam.setPassingMarks(rs.getInt("passing_marks"));
        Date examDate = rs.getDate("exam_date");
        Time examTime = rs.getTime("exam_time");
        if (examDate != null) {
            exam.setExamDate(examDate.toLocalDate());
        }
        if (examTime != null) {
            exam.setExamTime(examTime.toLocalTime());
        }
        exam.setPublished(rs.getBoolean("is_published"));
        return exam;
    }
}
