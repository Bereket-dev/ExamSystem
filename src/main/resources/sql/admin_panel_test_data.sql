-- Admin panel supplemental data
-- Canonical seed data is in test_data.sql — run that first on a fresh database.
-- This file only re-applies teacher-course links safely if the database already has users/courses.

USE exam_system;

INSERT IGNORE INTO teacher_courses (teacher_id, course_id, is_active)
SELECT t.teacher_id, c.course_id, TRUE
FROM teachers t
JOIN users u ON t.user_id = u.user_id
JOIN courses c ON c.course_code IN ('SWEG3102', 'SWEG3104', 'SWEG3106', 'SWEG3108', 'SWEG3110')
WHERE u.username = 'teacher1';

INSERT IGNORE INTO teacher_courses (teacher_id, course_id, is_active)
SELECT t.teacher_id, c.course_id, TRUE
FROM teachers t
JOIN users u ON t.user_id = u.user_id
JOIN courses c ON c.course_code IN ('SWEG3102', 'SWEG3104')
WHERE u.username = 'teacher2';
