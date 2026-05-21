-- Run once: sudo mysql < src/main/resources/sql/setup_user.sql
CREATE DATABASE IF NOT EXISTS exam_system;

CREATE USER IF NOT EXISTS 'examsystem'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON exam_system.* TO 'examsystem'@'localhost';
FLUSH PRIVILEGES;
