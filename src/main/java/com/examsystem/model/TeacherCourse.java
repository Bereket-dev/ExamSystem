package com.examsystem.model;

import java.time.LocalDateTime;

/**
 * TeacherCourse Model class representing the assignment of teachers to courses.
 */
public class TeacherCourse {
    private int teacherCourseId;
    private int teacherId;
    private int courseId;
    private LocalDateTime assignedDate;
    private LocalDateTime removedDate;
    private boolean isActive;

    // Constructors
    public TeacherCourse() {
    }

    public TeacherCourse(int teacherId, int courseId) {
        this.teacherId = teacherId;
        this.courseId = courseId;
        this.isActive = true;
    }

    // Getters and Setters
    public int getTeacherCourseId() {
        return teacherCourseId;
    }

    public void setTeacherCourseId(int teacherCourseId) {
        this.teacherCourseId = teacherCourseId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getRemovedDate() {
        return removedDate;
    }

    public void setRemovedDate(LocalDateTime removedDate) {
        this.removedDate = removedDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "TeacherCourse{" +
                "teacherCourseId=" + teacherCourseId +
                ", teacherId=" + teacherId +
                ", courseId=" + courseId +
                ", isActive=" + isActive +
                '}';
    }
}
