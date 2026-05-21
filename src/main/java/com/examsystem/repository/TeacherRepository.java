package com.examsystem.repository;

import com.examsystem.model.Teacher;

import java.util.Optional;

public interface TeacherRepository {
    Optional<Teacher> findByUserId(int userId);
}
