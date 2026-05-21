package com.examsystem.repository;

import com.examsystem.model.ExamAttempt;
import com.examsystem.model.ExamMonitoringEntry;
import com.examsystem.model.ExamReportEntry;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository {
    Optional<ExamAttempt> findByAssignmentId(int assignmentId);

    ExamAttempt createAttempt(int assignmentId);

    void updateAttempt(ExamAttempt attempt);

    List<ExamMonitoringEntry> findActiveAttemptsByTeacherId(int teacherId);

    List<ExamReportEntry> findSubmittedReportsByTeacherId(int teacherId);
}
