package com.examsystem.model;

/**
 * Student Model class for student-specific information.
 */
public class Student {
    private int studentId;
    private int userId;
    private String enrollmentNumber;
    private String department;
    private int semester;

    // Constructors
    public Student() {
    }

    public Student(int userId, String enrollmentNumber, String department, int semester) {
        this.userId = userId;
        this.enrollmentNumber = enrollmentNumber;
        this.department = department;
        this.semester = semester;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", enrollmentNumber='" + enrollmentNumber + '\'' +
                ", department='" + department + '\'' +
                ", semester=" + semester +
                '}';
    }
}
