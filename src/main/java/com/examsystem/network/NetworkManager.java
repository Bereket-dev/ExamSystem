package com.examsystem.network;

import com.examsystem.model.StudentAnswer;
import com.examsystem.network.client.ExamClient;
import com.examsystem.network.message.NetworkMessage;
import com.examsystem.network.server.ExamServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Central access point for TCP server and client lifecycle.
 */
public class NetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManager.class);
    private static NetworkManager instance;

    private ExamServer examServer;
    private ExamClient examClient;

    private NetworkManager() {
    }

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public synchronized void startServer() {
        if (examServer == null) {
            examServer = new ExamServer();
        }
        if (!examServer.isRunning()) {
            examServer.start();
        }
    }

    public synchronized void stopServer() {
        if (examServer != null) {
            examServer.stop();
        }
    }

    public boolean isServerRunning() {
        return examServer != null && examServer.isRunning();
    }

    public int getActiveClientCount() {
        return examServer != null ? examServer.getActiveClientCount() : 0;
    }

    public synchronized boolean connectClient() {
        if (examClient == null) {
            examClient = new ExamClient();
        }
        return examClient.connect();
    }

    public synchronized boolean connectClient(String host, int port) {
        examClient = new ExamClient(host, port);
        return examClient.connect();
    }

    public synchronized void disconnectClient() {
        if (examClient != null) {
            examClient.disconnect();
        }
    }

    public boolean isClientConnected() {
        return examClient != null && examClient.isConnected();
    }

    public NetworkMessage clientLogin(String username, String password) throws IOException {
        ensureClient();
        return examClient.login(username, password);
    }

    public void syncSaveAnswer(StudentAnswer answer) {
        if (!isClientConnected()) {
            return;
        }
        try {
            ExamClient.StudentAnswerPayload payload = new ExamClient.StudentAnswerPayload();
            payload.setAttemptId(answer.getAttemptId());
            payload.setQuestionId(answer.getQuestionId());
            payload.setSelectedOptionId(answer.getSelectedOptionId());
            payload.setShortAnswerText(answer.getShortAnswerText());
            payload.setCorrect(answer.isCorrect());
            payload.setMarksObtained(answer.getMarksObtained());
            examClient.saveAnswer(payload);
        } catch (IOException e) {
            logger.warn("Network save failed, local save retained: {}", e.getMessage());
        }
    }

    public void syncSubmitExam(int assignmentId, int totalMarks) {
        if (!isClientConnected()) {
            return;
        }
        try {
            examClient.submitExam(assignmentId, totalMarks);
        } catch (IOException e) {
            logger.warn("Network submit failed, local submit retained: {}", e.getMessage());
        }
    }

    public boolean pingServer() {
        try {
            ensureClient();
            NetworkMessage response = examClient.ping();
            return response != null && response.isSuccess();
        } catch (IOException e) {
            logger.warn("Ping failed: {}", e.getMessage());
            return false;
        }
    }

    public synchronized void shutdown() {
        stopServer();
        disconnectClient();
    }

    private void ensureClient() throws IOException {
        if (examClient == null) {
            examClient = new ExamClient();
        }
        if (!examClient.isConnected() && !examClient.connect()) {
            throw new IOException("Unable to connect to ExamServer");
        }
    }
}
