package com.examsystem.rmi.server;

import com.examsystem.model.StudentAnswer;
import com.examsystem.model.User;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;
import com.examsystem.rmi.remote.ExamRemoteService;
import com.examsystem.rmi.remote.LoginResult;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.rmi.remote.RemoteAnswerPayload;
import com.examsystem.service.StudentService;
import com.examsystem.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * RMI remote service implementation delegating to application services.
 */
public class ExamRemoteServiceImpl extends UnicastRemoteObject implements ExamRemoteService {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ExamRemoteServiceImpl.class);

    private final UserRepository userRepository = new UserRepositoryImpl();
    private final StudentService studentService = new StudentService();
    private final TeacherService teacherService = new TeacherService();
    private final Object saveLock = new Object();

    public ExamRemoteServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String ping() {
        return "PONG";
    }

    @Override
    public LoginResult login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return LoginResult.failure("Invalid credentials");
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            return LoginResult.failure("User account inactive");
        }
        return LoginResult.success(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name());
    }

    @Override
    public boolean saveAnswer(RemoteAnswerPayload payload) {
        synchronized (saveLock) {
            StudentAnswer answer = new StudentAnswer();
            answer.setAttemptId(payload.getAttemptId());
            answer.setQuestionId(payload.getQuestionId());
            answer.setSelectedOptionId(payload.getSelectedOptionId());
            answer.setShortAnswerText(payload.getShortAnswerText());
            answer.setCorrect(payload.getCorrect());
            answer.setMarksObtained(payload.getMarksObtained());
            studentService.saveStudentAnswer(answer);
            logger.debug("RMI saved answer for attempt {} question {}", payload.getAttemptId(),
                    payload.getQuestionId());
            return true;
        }
    }

    @Override
    public boolean submitExam(int assignmentId, int totalMarks) {
        var attemptOpt = studentService.getAttemptByAssignment(assignmentId);
        if (attemptOpt.isEmpty()) {
            return false;
        }
        var attempt = attemptOpt.get();
        attempt.setSubmissionStatus("submitted");
        attempt.setTotalMarksObtained(totalMarks);
        attempt.setEndTime(LocalDateTime.now());
        studentService.updateAttempt(attempt);
        studentService.markAssignmentAttempted(assignmentId);
        return true;
    }

    @Override
    public MonitoringSummary getMonitoringSummary(int teacherId) {
        int active = teacherService.getActiveMonitoring(teacherId).size();
        int reports = teacherService.getSubmittedReports(teacherId).size();
        return new MonitoringSummary(active, reports);
    }
}
