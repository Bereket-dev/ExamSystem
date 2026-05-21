package com.examsystem.network.server;

import com.examsystem.model.ExamMonitoringEntry;
import com.examsystem.model.ExamReportEntry;
import com.examsystem.model.StudentAnswer;
import com.examsystem.model.User;
import com.examsystem.network.message.MessageType;
import com.examsystem.network.message.NetworkMessage;
import com.examsystem.repository.UserRepository;
import com.examsystem.repository.UserRepositoryImpl;
import com.examsystem.service.StudentService;
import com.examsystem.service.TeacherService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Processes incoming TCP requests on the server.
 */
public class NetworkRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(NetworkRequestHandler.class);

    private final UserRepository userRepository = new UserRepositoryImpl();
    private final StudentService studentService = new StudentService();
    private final TeacherService teacherService = new TeacherService();

    public NetworkMessage handle(NetworkMessage request) {
        if (request == null || request.getType() == null) {
            return NetworkMessage.error(null, "Invalid request");
        }

        try {
            return switch (request.getType()) {
                case PING -> NetworkMessage.pong(request.getRequestId());
                case LOGIN -> handleLogin(request);
                case SAVE_ANSWER -> handleSaveAnswer(request);
                case SUBMIT_EXAM -> handleSubmitExam(request);
                case GET_ASSIGNED_EXAMS -> handleGetAssignedExams(request);
                case GET_MONITORING -> handleGetMonitoring(request);
                default -> NetworkMessage.error(request.getRequestId(), "Unsupported message type");
            };
        } catch (Exception e) {
            logger.error("Error handling network request {}", request.getType(), e);
            return NetworkMessage.error(request.getRequestId(), e.getMessage());
        }
    }

    private NetworkMessage handleLogin(NetworkMessage request) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return NetworkMessage.error(request.getRequestId(), "Login payload required");
        }

        String username = payload.get("username").getAsString();
        String password = payload.get("password").getAsString();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return NetworkMessage.error(request.getRequestId(), "Invalid credentials");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            return NetworkMessage.error(request.getRequestId(), "User account inactive");
        }

        NetworkMessage response = new NetworkMessage(MessageType.LOGIN_RESPONSE);
        response.setRequestId(request.getRequestId());
        JsonObject responsePayload = new JsonObject();
        responsePayload.addProperty("userId", user.getUserId());
        responsePayload.addProperty("username", user.getUsername());
        responsePayload.addProperty("fullName", user.getFullName());
        responsePayload.addProperty("role", user.getRole().name());
        response.setPayload(responsePayload);
        return response;
    }

    private NetworkMessage handleSaveAnswer(NetworkMessage request) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return NetworkMessage.error(request.getRequestId(), "Save answer payload required");
        }

        StudentAnswer answer = new StudentAnswer();
        answer.setAttemptId(payload.get("attemptId").getAsInt());
        answer.setQuestionId(payload.get("questionId").getAsInt());
        if (payload.has("selectedOptionId") && !payload.get("selectedOptionId").isJsonNull()) {
            answer.setSelectedOptionId(payload.get("selectedOptionId").getAsInt());
        }
        if (payload.has("shortAnswerText") && !payload.get("shortAnswerText").isJsonNull()) {
            answer.setShortAnswerText(payload.get("shortAnswerText").getAsString());
        }
        if (payload.has("correct") && !payload.get("correct").isJsonNull()) {
            answer.setCorrect(payload.get("correct").getAsBoolean());
        }
        answer.setMarksObtained(payload.has("marksObtained") ? payload.get("marksObtained").getAsInt() : 0);

        studentService.saveStudentAnswer(answer);

        NetworkMessage response = new NetworkMessage(MessageType.ACK);
        response.setRequestId(request.getRequestId());
        return response;
    }

    private NetworkMessage handleSubmitExam(NetworkMessage request) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return NetworkMessage.error(request.getRequestId(), "Submit payload required");
        }

        int assignmentId = payload.get("assignmentId").getAsInt();
        int totalMarks = payload.get("totalMarks").getAsInt();

        var attemptOpt = studentService.getAttemptByAssignment(assignmentId);
        if (attemptOpt.isEmpty()) {
            return NetworkMessage.error(request.getRequestId(), "Attempt not found");
        }

        var attempt = attemptOpt.get();
        attempt.setSubmissionStatus("submitted");
        attempt.setTotalMarksObtained(totalMarks);
        attempt.setEndTime(java.time.LocalDateTime.now());
        studentService.updateAttempt(attempt);
        studentService.markAssignmentAttempted(assignmentId);

        NetworkMessage response = new NetworkMessage(MessageType.ACK);
        response.setRequestId(request.getRequestId());
        return response;
    }

    private NetworkMessage handleGetAssignedExams(NetworkMessage request) {
        JsonObject payload = request.getPayload();
        if (payload == null || !payload.has("studentId")) {
            return NetworkMessage.error(request.getRequestId(), "studentId required");
        }

        int studentId = payload.get("studentId").getAsInt();
        var assigned = studentService.getAssignedExamsWithStatus(studentId);

        NetworkMessage response = new NetworkMessage(MessageType.ACK);
        response.setRequestId(request.getRequestId());
        JsonObject responsePayload = new JsonObject();
        responsePayload.addProperty("count", assigned.size());
        response.setPayload(responsePayload);
        return response;
    }

    private NetworkMessage handleGetMonitoring(NetworkMessage request) {
        JsonObject payload = request.getPayload();
        if (payload == null || !payload.has("teacherId")) {
            return NetworkMessage.error(request.getRequestId(), "teacherId required");
        }

        int teacherId = payload.get("teacherId").getAsInt();
        List<ExamMonitoringEntry> active = teacherService.getActiveMonitoring(teacherId);
        List<ExamReportEntry> reports = teacherService.getSubmittedReports(teacherId);

        NetworkMessage response = new NetworkMessage(MessageType.ACK);
        response.setRequestId(request.getRequestId());
        JsonObject responsePayload = new JsonObject();
        responsePayload.addProperty("activeCount", active.size());
        responsePayload.addProperty("reportCount", reports.size());
        response.setPayload(responsePayload);
        return response;
    }
}
