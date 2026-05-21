-- ExamSystem Test Data
-- Phase 1 Test Data Setup

USE exam_system;

-- Insert test users
INSERT INTO users (username, password, email, full_name, role) VALUES
('admin', 'admin123', 'admin@examsystem.com', 'System Administrator', 'admin'),
('teacher1', 'teacher123', 'teacher1@examsystem.com', 'Dr. John Smith', 'teacher'),
('teacher2', 'teacher123', 'teacher2@examsystem.com', 'Prof. Jane Doe', 'teacher'),
('student1', 'student123', 'student1@examsystem.com', 'Alice Johnson', 'student'),
('student2', 'student123', 'student2@examsystem.com', 'Bob Wilson', 'student'),
('student3', 'student123', 'student3@examsystem.com', 'Carol Davis', 'student');

-- Insert teacher details
INSERT INTO teachers (user_id, department, qualification, experience_years) VALUES
(2, 'Computer Science', 'PhD in Computer Science', 10),
(3, 'Computer Science', 'MTech in Software Engineering', 8);

-- Insert student details
INSERT INTO students (user_id, enrollment_number, department, semester) VALUES
(4, 'CS2021001', 'Computer Science', 4),
(5, 'CS2021002', 'Computer Science', 4),
(6, 'CS2021003', 'Computer Science', 4);

-- Insert sample exam
INSERT INTO exams (teacher_id, exam_name, description, subject, duration_minutes, total_questions, total_marks, passing_marks, exam_date, exam_time, is_published)
VALUES
(1, 'Java Programming Final Exam', 'Final examination for Java Programming course', 'Java Programming', 120, 50, 100, 40, '2024-06-15', '10:00:00', TRUE),
(1, 'Database Management System', 'DBMS course assessment', 'Database Systems', 90, 40, 80, 32, '2024-06-20', '14:00:00', TRUE);

-- Insert sample questions (for Java exam)
INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(1, 'What is the primary purpose of the JVM?', 'mcq', 2, 1, 'easy'),
(1, 'Which of the following is NOT a primitive data type in Java?', 'mcq', 2, 2, 'easy'),
(1, 'True or False: Java supports multiple inheritance.', 'true_false', 1, 3, 'medium'),
(1, 'Explain the concept of encapsulation in OOP.', 'short_answer', 5, 4, 'hard');

-- Insert options for questions
INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
(1, 'To compile Java code', FALSE, 1),
(1, 'To provide a platform-independent way to run Java code', TRUE, 2),
(1, 'To manage memory allocation', FALSE, 3),
(1, 'To parse Java syntax', FALSE, 4),
(2, 'int', FALSE, 1),
(2, 'String', TRUE, 2),
(2, 'boolean', FALSE, 3),
(2, 'double', FALSE, 4),
(3, 'True', FALSE, 1),
(3, 'False', TRUE, 2);

-- Assign exams to students
INSERT INTO student_exam_assignments (exam_id, student_id, assigned_date, is_attempted) VALUES
(1, 1, NOW(), FALSE),
(1, 2, NOW(), FALSE),
(1, 3, NOW(), FALSE),
(2, 1, NOW(), FALSE),
(2, 2, NOW(), FALSE);
