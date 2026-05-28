package com.examsystem.rmi.server;

import com.examsystem.model.StudentAnswer;
import com.examsystem.model.User;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;
import com.examsystem.rmi.remote.ClientPresenceResult;
import com.examsystem.rmi.remote.ExamRemoteService;
import com.examsystem.rmi.remote.LoginResult;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.rmi.remote.RemoteAnswerPayload;
import com.examsystem.db.DatabaseConnection;
import com.examsystem.service.StudentService;
import com.examsystem.service.TeacherService;
import com.examsystem.sync.ClientConnectionEvent;
import com.examsystem.sync.ClientSessionRegistry;
import com.examsystem.sync.DatabaseSyncService;
import com.examsystem.rmi.remote.SyncBundle;
import com.examsystem.rmi.remote.SyncResult;
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
    private final DatabaseSyncService databaseSyncService = new DatabaseSyncService();
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
    public ClientPresenceResult registerClientPresence(String username, String role) {
        logger.info("[SERVER-PRESENCE] Received presence registration: username={}, role={}", username, role);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            String msg = "Unknown user: " + username;
            logger.warn("[SERVER-PRESENCE] Registration failed: {}", msg);
            return ClientPresenceResult.fail(msg);
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            String msg = "User account is inactive";
            logger.warn("[SERVER-PRESENCE] Registration failed: {}", msg);
            return ClientPresenceResult.fail(msg);
        }
        if (user.getRole() == User.UserRole.ADMIN) {
            String msg = "Admin accounts do not register as clients";
            logger.warn("[SERVER-PRESENCE] Registration failed: {}", msg);
            return ClientPresenceResult.fail(msg);
        }
        String roleNorm = role == null ? "" : role.trim().toUpperCase();
        if (!roleNorm.isEmpty() && !user.getRole().name().equalsIgnoreCase(roleNorm)) {
            String msg = "Role mismatch for user " + username;
            logger.warn("[SERVER-PRESENCE] Registration failed: {}", msg);
            return ClientPresenceResult.fail(msg);
        }
        logger.debug("[SERVER-PRESENCE] All validation passed, registering in ClientSessionRegistry");
        ClientConnectionEvent event = ClientSessionRegistry.getInstance().registerClientConnection(
                user.getUsername(), user.getFullName(), user.getRole().name());
        logger.info("[SERVER-PRESENCE] Client presence registered successfully: {} — {}", username, event.message());
        return ClientPresenceResult.ok(event.message());
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

    @Override
    public SyncBundle pullSyncBundle() throws RemoteException {
        registerClientHeartbeat();
        try (var central = DatabaseConnection.getCentralConnection()) {
            return databaseSyncService.exportAll(central);
        } catch (Exception e) {
            logger.error("RMI pullSyncBundle failed", e);
            throw new RemoteException("Failed to export central data: " + e.getMessage(), e);
        }
    }

    @Override
    public SyncResult pushSyncBundle(SyncBundle bundle) throws RemoteException {
        registerClientHeartbeat();
        try (var central = DatabaseConnection.getCentralConnection()) {
            return databaseSyncService.importAll(central, bundle, false);
        } catch (Exception e) {
            logger.error("RMI pushSyncBundle failed", e);
            throw new RemoteException("Failed to import to central: " + e.getMessage(), e);
        }
    }

    private void registerClientHeartbeat() {
        ClientSessionRegistry.getInstance().heartbeat("rmi-sync-" + System.currentTimeMillis());
    }
}
