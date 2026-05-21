package com.examsystem.service;

import com.examsystem.model.Exam;
import com.examsystem.model.ExamMonitoringEntry;
import com.examsystem.model.ExamReportEntry;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import com.examsystem.model.Student;
import com.examsystem.model.Teacher;
import com.examsystem.repository.ExamAttemptRepository;
import com.examsystem.repository.ExamAttemptRepositoryImpl;
import com.examsystem.repository.ExamRepository;
import com.examsystem.repository.ExamRepositoryImpl;
import com.examsystem.repository.QuestionRepository;
import com.examsystem.repository.QuestionRepositoryImpl;
import com.examsystem.repository.StudentRepository;
import com.examsystem.repository.StudentRepositoryImpl;
import com.examsystem.repository.TeacherRepository;
import com.examsystem.repository.TeacherRepositoryImpl;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for teacher module (Phase 5).
 */
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;
    private final ExamAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public TeacherService() {
        this.teacherRepository = new TeacherRepositoryImpl();
        this.examRepository = new ExamRepositoryImpl();
        this.questionRepository = new QuestionRepositoryImpl();
        this.studentRepository = new StudentRepositoryImpl();
        this.attemptRepository = new ExamAttemptRepositoryImpl();
        this.userRepository = new UserRepositoryImpl();
    }

    public Optional<Teacher> findByUserId(int userId) {
        return teacherRepository.findByUserId(userId);
    }

    public List<Exam> getExamsByTeacher(int teacherId) {
        return examRepository.findByTeacherId(teacherId);
    }

    public Exam createExam(Exam exam) {
        examRepository.save(exam);
        return exam;
    }

    public void publishExam(int examId, boolean publish) {
        examRepository.setPublished(examId, publish);
    }

    public Question addQuestion(Question question) {
        questionRepository.saveQuestion(question);
        int count = questionRepository.countByExamId(question.getExamId());
        Exam exam = examRepository.findById(question.getExamId()).orElse(null);
        if (exam != null) {
            exam.setTotalQuestions(count);
            examRepository.update(exam);
        }
        return question;
    }

    public void addOption(QuestionOption option) {
        questionRepository.saveOption(option);
    }

    public List<Question> getQuestions(int examId) {
        return questionRepository.findByExamId(examId);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public String getStudentDisplayName(Student student) {
        return userRepository.findById(student.getUserId())
                .map(user -> user.getFullName() + " (" + student.getEnrollmentNumber() + ")")
                .orElse(student.getEnrollmentNumber());
    }

    public void assignExamToStudent(int examId, int studentId) {
        studentRepository.assignExamToStudent(examId, studentId);
    }

    public List<ExamMonitoringEntry> getActiveMonitoring(int teacherId) {
        return attemptRepository.findActiveAttemptsByTeacherId(teacherId);
    }

    public List<ExamReportEntry> getSubmittedReports(int teacherId) {
        return attemptRepository.findSubmittedReportsByTeacherId(teacherId);
    }
}
