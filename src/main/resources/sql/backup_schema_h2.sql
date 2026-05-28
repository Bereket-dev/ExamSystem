-- Local backup database schema (H2) — mirrors central MySQL tables for offline use

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY,
    user_id INT NOT NULL,
    enrollment_number VARCHAR(20) NOT NULL,
    department VARCHAR(50),
    semester INT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT PRIMARY KEY,
    user_id INT NOT NULL,
    department VARCHAR(50),
    qualification VARCHAR(100),
    experience_years INT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description CLOB,
    department VARCHAR(50),
    credits INT DEFAULT 3,
    semester INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS teacher_courses (
    teacher_course_id INT PRIMARY KEY,
    teacher_id INT NOT NULL,
    course_id INT NOT NULL,
    assigned_date TIMESTAMP,
    removed_date TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exams (
    exam_id INT PRIMARY KEY,
    teacher_id INT NOT NULL,
    exam_name VARCHAR(100) NOT NULL,
    description CLOB,
    subject VARCHAR(50) NOT NULL,
    course_id INT,
    duration_minutes INT NOT NULL,
    total_questions INT NOT NULL,
    total_marks INT NOT NULL,
    passing_marks INT,
    exam_date DATE,
    exam_time TIME,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
    question_id INT PRIMARY KEY,
    exam_id INT NOT NULL,
    question_text CLOB NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    marks INT NOT NULL DEFAULT 1,
    sequence_order INT NOT NULL,
    difficulty_level VARCHAR(20) DEFAULT 'medium',
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS options (
    option_id INT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text CLOB NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    sequence_order INT NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_exam_assignments (
    assignment_id INT PRIMARY KEY,
    exam_id INT NOT NULL,
    student_id INT NOT NULL,
    assigned_date TIMESTAMP,
    is_attempted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS exam_attempts (
    attempt_id INT PRIMARY KEY,
    assignment_id INT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_marks_obtained INT,
    submission_status VARCHAR(20) DEFAULT 'in_progress',
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_answers (
    answer_id INT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT,
    short_answer_text CLOB,
    is_correct BOOLEAN,
    marks_obtained INT DEFAULT 0,
    answer_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sync_queue (
    queue_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_key VARCHAR(100),
    operation VARCHAR(20) NOT NULL,
    payload CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sync_metadata (
    meta_key VARCHAR(50) PRIMARY KEY,
    meta_value VARCHAR(255)
);
