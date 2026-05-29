USE exam_system;



INSERT INTO users (username, password, email, full_name, role, is_active) VALUES
('admin', 'admin123', 'admin@examsystem.com', 'System Administrator', 'admin', TRUE),
('teacher1', 'teacher123', 'teacher1@examsystem.com', 'Dr. John Smith', 'teacher', TRUE),
('teacher2', 'teacher123', 'teacher2@examsystem.com', 'Prof. Jane Doe', 'teacher', TRUE),
('student1', 'student123', 'student1@examsystem.com', 'Alice Johnson', 'student', TRUE),
('student2', 'student123', 'student2@examsystem.com', 'Bob Wilson', 'student', TRUE),
('student3', 'student123', 'student3@examsystem.com', 'Carol Davis', 'student', TRUE);


INSERT INTO teachers (user_id, department, qualification, experience_years) VALUES
(2, 'Software Engineering', 'PhD in Software Engineering', 10),
(3, 'Software Engineering', 'MSc in Computer Science', 8);



INSERT INTO students (user_id, enrollment_number, department, semester) VALUES
(4, 'SE2021001', 'Software Engineering', 5),
(5, 'SE2021002', 'Software Engineering', 5),
(6, 'SE2021003', 'Software Engineering', 5);



INSERT INTO courses (course_code, course_name, description, department, credits, semester, is_active) VALUES
('SWEG3102', 'Internet Programming II (IP II)', 'Server-side scripting, HTML forms, file handling, database connectivity, session management, cookies, and CMS fundamentals.', 'Software Engineering', 3, 1, TRUE),
('SWEG3104', 'Software Requirements Engineering (SRE)', 'Requirements elicitation, analysis, specification, validation, and management. Focus on working with clients and understanding user needs.', 'Software Engineering', 3, 1, TRUE),
('SWEG3106', 'Operating Systems (OS)', 'OS design and implementation: synchronization, scheduling, deadlocks, paging, virtual memory, I/O devices, and file systems.', 'Software Engineering', 4, 1, TRUE),
('SWEG3108', 'Advanced Programming (AP)', 'Multithreading, socket programming, object serialization, RMI, database connectivity, GUI development, and web programming.', 'Software Engineering', 4, 1, TRUE),
('SWEG3110', 'Formal Language and Automata Theory (FLAT)', 'Strings, languages, grammar, finite automata, regular expressions, pushdown automata, context-free grammar, Turing machines.', 'Software Engineering', 3, 1, TRUE);


INSERT INTO teacher_courses (teacher_id, course_id, is_active) VALUES
(1, 1, TRUE),
(1, 2, TRUE),
(1, 3, TRUE),
(1, 4, TRUE),
(1, 5, TRUE),
(2, 1, TRUE),
(2, 2, TRUE);


INSERT INTO exams (teacher_id, course_id, exam_name, description, subject, duration_minutes, total_questions, total_marks, passing_marks, is_published) VALUES
(1, 1, 'Internet Programming II Final Exam', 'Final examination for Internet Programming II course', 'IP II', 60, 10, 20, 10, TRUE),
(1, 2, 'Software Requirements Engineering Final Exam', 'Final examination for Software Requirements Engineering', 'SRE', 60, 10, 20, 10, TRUE),
(1, 3, 'Operating Systems Final Exam', 'Final examination for Operating Systems', 'OS', 60, 10, 20, 10, TRUE),
(1, 4, 'Advanced Programming Final Exam', 'Final examination for Advanced Programming', 'AP', 60, 10, 20, 10, TRUE),
(1, 5, 'Formal Language and Automata Theory Final Exam', 'Final examination for FLAT', 'FLAT', 60, 10, 20, 10, TRUE);



INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(4, 'What does RPC stand for?', 'mcq', 2, 1, 'easy'),
(4, 'Which Java feature allows methods to be called on remote objects?', 'mcq', 2, 2, 'easy'),
(4, 'Which interface must a remote interface extend in Java RMI?', 'mcq', 2, 3, 'easy'),
(4, 'What is the default port number of the RMI Registry?', 'mcq', 2, 4, 'easy'),
(4, 'Which class is used to create a server socket in Java?', 'mcq', 2, 5, 'easy'),
(4, 'TCP guarantees delivery of packets in the correct order.', 'true_false', 2, 6, 'easy'),
(4, 'UDP is slower but more reliable than TCP.', 'true_false', 2, 7, 'easy');

INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 1), 'Remote Program Control', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 1), 'Remote Procedure Call', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 1), 'Remote Process Connection', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 1), 'Resource Program Call', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 2), 'JDBC', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 2), 'JVM', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 2), 'RMI', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 2), 'JDK', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 3), 'Serializable', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 3), 'Runnable', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 3), 'Remote', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 3), 'Cloneable', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 4), '8080', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 4), '3306', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 4), '1099', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 4), '21', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 5), 'Socket', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 5), 'ServerSocket', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 5), 'DatagramSocket', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 5), 'URLConnection', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 6), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 6), 'False', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 7), 'True', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 4 AND sequence_order = 7), 'False', TRUE, 2);

-- =============================================
-- 8. QUESTIONS FOR IP II (Internet Programming II) - Exam ID: 1
-- =============================================

INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(1, 'Which keyword is used to create an object in PHP?', 'mcq', 2, 1, 'easy'),
(1, 'Which access modifier allows access only inside the same class?', 'mcq', 2, 2, 'easy'),
(1, 'Which keyword is used for inheritance in PHP?', 'mcq', 2, 3, 'easy'),
(1, 'Which function is used to automatically load classes in PHP?', 'mcq', 2, 4, 'easy'),
(1, 'In MVC architecture, which component handles user interface display?', 'mcq', 2, 5, 'easy'),
(1, 'Which Laravel feature allows database interaction using PHP objects?', 'mcq', 2, 6, 'easy'),
(1, 'A constructor in PHP is defined using __construct().', 'true_false', 2, 7, 'easy'),
(1, 'CodeIgniter is heavier and slower than Laravel.', 'true_false', 2, 8, 'easy'),
(1, 'The unlink() function is used to delete a file in PHP.', 'true_false', 2, 9, 'easy');

INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 1), 'class', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 1), 'function', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 1), 'new', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 1), 'object', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 2), 'public', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 2), 'protected', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 2), 'private', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 2), 'static', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 3), 'include', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 3), 'extends', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 3), 'implement', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 3), 'inherit', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 4), 'include()', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 4), 'require()', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 4), 'spl_autoload_register()', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 4), 'fopen()', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 5), 'Model', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 5), 'View', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 5), 'Controller', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 5), 'Route', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 6), 'Blade', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 6), 'Routing', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 6), 'Eloquent ORM', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 6), 'Middleware', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 7), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 7), 'False', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 8), 'True', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 8), 'False', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 9), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 1 AND sequence_order = 9), 'False', FALSE, 2);

-- =============================================
-- 9. QUESTIONS FOR FLAT - Exam ID: 5
-- =============================================

INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(5, 'A grammar is context-free if:', 'mcq', 2, 1, 'medium'),
(5, 'Which language is context-free but not regular?', 'mcq', 2, 2, 'medium'),
(5, 'Which of the following is in Chomsky Normal Form (CNF)?', 'mcq', 2, 3, 'medium'),
(5, 'A grammar is ambiguous if:', 'mcq', 2, 4, 'medium'),
(5, 'Every regular language is context-free.', 'true_false', 2, 5, 'easy');

INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 1), 'Every rule has one terminal on the left side', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 1), 'Every rule has one non-terminal on the left side', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 1), 'Every rule has two variables', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 1), 'Every rule contains ε', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 2), '{a*b*}', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 2), '{ab}', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 2), '{aⁿbⁿ | n ≥ 1}', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 2), '{a,b}*', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 3), 'A → aB', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 3), 'A → BC', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 3), 'A → BCD', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 3), 'A → εB', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 4), 'It has useless symbols', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 4), 'It has ε-productions', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 4), 'One string has more than one parse tree', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 4), 'It has unit productions', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 5), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 5 AND sequence_order = 5), 'False', FALSE, 2);

-- =============================================
-- 10. QUESTIONS FOR OS (Operating Systems) - Exam ID: 3
-- =============================================

INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(3, 'A process is:', 'mcq', 2, 1, 'easy'),
(3, 'Which scheduling algorithm uses time quantum?', 'mcq', 2, 2, 'easy'),
(3, 'Threads of the same process share:', 'mcq', 2, 3, 'medium'),
(3, 'Banker''s Algorithm is used for:', 'mcq', 2, 4, 'hard'),
(3, 'Every process has its own memory space.', 'true_false', 2, 5, 'easy');

INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 1), 'A file stored in disk', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 1), 'A program in execution', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 1), 'A compiler', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 1), 'A thread', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 2), 'FCFS', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 2), 'SJF', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 2), 'Round Robin', TRUE, 3),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 2), 'Priority Scheduling', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 3), 'Stack', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 3), 'Registers', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 3), 'Program counter', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 3), 'Code and data section', TRUE, 4),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 4), 'CPU Scheduling', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 4), 'Deadlock Avoidance', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 4), 'Paging', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 4), 'Multithreading', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 5), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 3 AND sequence_order = 5), 'False', FALSE, 2);

-- =============================================
-- 11. QUESTIONS FOR SRE (Software Requirements Engineering) - Exam ID: 2
-- =============================================

INSERT INTO questions (exam_id, question_text, question_type, marks, sequence_order, difficulty_level) VALUES
(2, 'Non-functional requirements describe:', 'mcq', 2, 1, 'easy'),
(2, 'Which model gathers requirements through customer interaction repeatedly?', 'mcq', 2, 2, 'medium'),
(2, 'Feasibility study checks:', 'mcq', 2, 3, 'easy'),
(2, 'UML stands for:', 'mcq', 2, 4, 'easy'),
(2, 'Use case diagrams are mainly used to:', 'mcq', 2, 5, 'easy'),
(2, 'Non-functional requirements describe system performance and security.', 'true_false', 2, 6, 'easy');

INSERT INTO options (question_id, option_text, is_correct, sequence_order) VALUES
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 1), 'Algorithms only', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 1), 'Quality attributes of the system', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 1), 'Source code', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 1), 'Data structures', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 2), 'Waterfall Model', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 2), 'Prototype Model', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 2), 'Spiral Model', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 2), 'Incremental Model', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 3), 'Only coding speed', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 3), 'Whether the project is practical and possible', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 3), 'Only hardware', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 3), 'Only software testing', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 4), 'Unified Modeling Language', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 4), 'Universal Machine Language', FALSE, 2),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 4), 'User Method Language', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 4), 'Unified Method Logic', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 5), 'Design database', FALSE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 5), 'Show user interaction with the system', TRUE, 2),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 5), 'Write source code', FALSE, 3),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 5), 'Manage memory', FALSE, 4),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 6), 'True', TRUE, 1),
((SELECT question_id FROM questions WHERE exam_id = 2 AND sequence_order = 6), 'False', FALSE, 2);

-- =============================================
-- 12. ASSIGN EXAMS TO STUDENTS
-- =============================================

INSERT INTO student_exam_assignments (exam_id, student_id, assigned_date, is_attempted) VALUES
(1, 1, NOW(), FALSE),
(1, 2, NOW(), FALSE),
(1, 3, NOW(), FALSE),
(2, 1, NOW(), FALSE),
(2, 2, NOW(), FALSE),
(2, 3, NOW(), FALSE),
(3, 1, NOW(), FALSE),
(3, 2, NOW(), FALSE),
(3, 3, NOW(), FALSE),
(4, 1, NOW(), FALSE),
(4, 2, NOW(), FALSE),
(4, 3, NOW(), FALSE),
(5, 1, NOW(), FALSE),
(5, 2, NOW(), FALSE),
(5, 3, NOW(), FALSE);
