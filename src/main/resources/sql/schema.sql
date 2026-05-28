-- ExamSystem Database Schema
-- Phase 1 SQL Setup

CREATE DATABASE IF NOT EXISTS exam_system;
USE exam_system;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL DEFAULT 'student',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    enrollment_number VARCHAR(20) UNIQUE NOT NULL,
    department VARCHAR(50),
    semester INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_enrollment (enrollment_number)
);

-- Teachers Table
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(50),
    qualification VARCHAR(100),
    experience_years INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Courses Table (must exist before exams.teacher_id -> course_id FK)
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    department VARCHAR(50),
    credits INT DEFAULT 3,
    semester INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_code (course_code),
    INDEX idx_department (department),
    INDEX idx_semester (semester),
    INDEX idx_active (is_active)
);

-- Teacher-Course Assignments
CREATE TABLE IF NOT EXISTS teacher_courses (
    teacher_course_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    course_id INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    removed_date TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_teacher_course (teacher_id, course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_course_id (course_id),
    INDEX idx_active (is_active)
);

-- Exams Table
CREATE TABLE IF NOT EXISTS exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    exam_name VARCHAR(100) NOT NULL,
    description TEXT,
    subject VARCHAR(50) NOT NULL,
    course_id INT,
    duration_minutes INT NOT NULL,
    total_questions INT NOT NULL,
    total_marks INT NOT NULL,
    passing_marks INT,
    exam_date DATE,
    exam_time TIME,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL,
    INDEX idx_exam_teacher (teacher_id),
    INDEX idx_exam_course (course_id),
    INDEX idx_exam_date (exam_date),
    INDEX idx_published (is_published)
);

-- Questions Table
CREATE TABLE IF NOT EXISTS questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    question_text LONGTEXT NOT NULL,
    question_type ENUM('mcq', 'true_false', 'short_answer') NOT NULL,
    marks INT NOT NULL DEFAULT 1,
    sequence_order INT NOT NULL,
    difficulty_level ENUM('easy', 'medium', 'hard') DEFAULT 'medium',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    INDEX idx_exam_id (exam_id),
    INDEX idx_sequence (exam_id, sequence_order)
);

-- Options Table (for MCQ and True/False)
CREATE TABLE IF NOT EXISTS options (
    option_id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    sequence_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_correct (is_correct)
);

-- Student Exam Assignments
CREATE TABLE IF NOT EXISTS student_exam_assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    student_id INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_attempted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY unique_assignment (exam_id, student_id),
    INDEX idx_student_id (student_id)
);

-- Exam Attempts Table
CREATE TABLE IF NOT EXISTS exam_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    assignment_id INT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_marks_obtained INT,
    submission_status ENUM('in_progress', 'submitted', 'abandoned') DEFAULT 'in_progress',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES student_exam_assignments(assignment_id) ON DELETE CASCADE,
    INDEX idx_assignment_id (assignment_id),
    INDEX idx_status (submission_status)
);

-- Student Answers Table
CREATE TABLE IF NOT EXISTS student_answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT,
    short_answer_text TEXT,
    is_correct BOOLEAN,
    marks_obtained INT DEFAULT 0,
    answer_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES options(option_id) ON DELETE SET NULL,
    INDEX idx_attempt_id (attempt_id),
    INDEX idx_correct (is_correct)
);

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    old_value LONGTEXT,
    new_value LONGTEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
);
