-- Test data setup for Admin Panel
-- Add test admin, teachers, students, and courses

-- Add admin user if not exists
INSERT IGNORE INTO users (username, password, email, full_name, role, is_active)
VALUES ('admin', 'admin123', 'admin@examsystem.edu', 'System Administrator', 'admin', TRUE);

-- Add sample teachers
INSERT IGNORE INTO users (username, password, email, full_name, role, is_active)
VALUES 
('teacher1', 'teacher123', 'teacher1@examsystem.edu', 'Dr. John Smith', 'teacher', TRUE),
('teacher2', 'teacher123', 'teacher2@examsystem.edu', 'Dr. Sarah Johnson', 'teacher', TRUE),
('teacher3', 'teacher123', 'teacher3@examsystem.edu', 'Prof. Michael Brown', 'teacher', TRUE);

-- Add teacher profiles
INSERT IGNORE INTO teachers (user_id, department, qualification, experience_years)
SELECT user_id, 'Computer Science', 'Ph.D', 8 FROM users WHERE username = 'teacher1'
UNION ALL
SELECT user_id, 'Computer Science', 'M.Tech', 5 FROM users WHERE username = 'teacher2'
UNION ALL
SELECT user_id, 'Information Technology', 'M.Tech', 6 FROM users WHERE username = 'teacher3';

-- Add sample students
INSERT IGNORE INTO users (username, password, email, full_name, role, is_active)
VALUES 
('student1', 'student123', 'student1@examsystem.edu', 'Alice Williams', 'student', TRUE),
('student2', 'student123', 'student2@examsystem.edu', 'Bob Davis', 'student', TRUE),
('student3', 'student123', 'student3@examsystem.edu', 'Carol Martinez', 'student', TRUE);

-- Add student profiles
INSERT IGNORE INTO students (user_id, enrollment_number, department, semester)
SELECT user_id, 'EN001', 'Computer Science', 2 FROM users WHERE username = 'student1'
UNION ALL
SELECT user_id, 'EN002', 'Computer Science', 2 FROM users WHERE username = 'student2'
UNION ALL
SELECT user_id, 'EN003', 'Information Technology', 3 FROM users WHERE username = 'student3';

-- Add sample courses
INSERT IGNORE INTO courses (course_code, course_name, description, department, credits, semester, is_active)
VALUES 
('CS101', 'Introduction to Programming', 'Basic programming concepts and fundamentals', 'Computer Science', 4, 1, TRUE),
('CS201', 'Data Structures', 'Study of data structures and algorithms', 'Computer Science', 4, 2, TRUE),
('CS301', 'Database Management Systems', 'DBMS concepts and SQL', 'Computer Science', 3, 3, TRUE),
('IT101', 'Web Development Basics', 'HTML, CSS, and JavaScript fundamentals', 'Information Technology', 3, 1, TRUE),
('IT201', 'Advanced Web Development', 'React, Node.js, and modern frameworks', 'Information Technology', 4, 2, TRUE);

-- Assign teachers to courses (if teacher_courses table exists)
INSERT IGNORE INTO teacher_courses (teacher_id, course_id, is_active)
SELECT t.teacher_id, c.course_id, TRUE 
FROM teachers t, courses c 
WHERE t.user_id IN (SELECT user_id FROM users WHERE username = 'teacher1')
AND c.course_code IN ('CS101', 'CS201', 'CS301')
UNION ALL
SELECT t.teacher_id, c.course_id, TRUE 
FROM teachers t, courses c 
WHERE t.user_id IN (SELECT user_id FROM users WHERE username = 'teacher2')
AND c.course_code IN ('CS201')
UNION ALL
SELECT t.teacher_id, c.course_id, TRUE 
FROM teachers t, courses c 
WHERE t.user_id IN (SELECT user_id FROM users WHERE username = 'teacher3')
AND c.course_code IN ('IT101', 'IT201');
