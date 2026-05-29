package com.examsystem.network.client;

import com.examsystem.network.message.MessageType;
import com.examsystem.network.message.NetworkMessage;
import com.examsystem.util.ConfigManager;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class ExamClient {
    private static final Logger logger = LoggerFactory.getLogger(ExamClient.class);

    private final String host;
    private final int port;
    private final int maxReconnectAttempts;
    private final long reconnectDelayMs;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Object ioLock = new Object();

    public ExamClient() {
        this.host = ConfigManager.getProperty("network.server.host", "localhost");
        this.port = ConfigManager.getIntProperty("network.server.port", 5000);
        this.maxReconnectAttempts = ConfigManager.getIntProperty("network.client.reconnect.max", 3);
        this.reconnectDelayMs = ConfigManager.getIntProperty("network.client.reconnect.delay.ms", 2000);
    }

    public ExamClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.maxReconnectAttempts = ConfigManager.getIntProperty("network.client.reconnect.max", 3);
        this.reconnectDelayMs = ConfigManager.getIntProperty("network.client.reconnect.delay.ms", 2000);
    }

    public boolean connect() {
        return connectWithRetry(maxReconnectAttempts);
    }

    public boolean connectWithRetry(int attempts) {
        disconnect();
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                socket = new Socket(host, port);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                connected.set(true);
                logger.info("Connected to ExamServer at {}:{}", host, port);
                return true;
            } catch (IOException e) {
                logger.warn("Connection attempt {}/{} failed: {}", attempt, attempts, e.getMessage());
                disconnect();
                if (attempt < attempts) {
                    sleep(reconnectDelayMs);
                }
            }
        }
        return false;
    }

    public void disconnect() {
        connected.set(false);
        synchronized (ioLock) {
            closeQuietly(reader);
            closeQuietly(writer);
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.debug("Error closing client socket", e);
                }
            }
            socket = null;
            reader = null;
            writer = null;
        }
    }

    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    public NetworkMessage send(NetworkMessage request) throws IOException {
        if (!isConnected() && !connectWithRetry(maxReconnectAttempts)) {
            throw new IOException("Not connected to ExamServer");
        }

        if (request.getRequestId() == null || request.getRequestId().isBlank()) {
            request.setRequestId(UUID.randomUUID().toString());
        }

        synchronized (ioLock) {
            try {
                writer.println(request.toJson());
                String responseLine = reader.readLine();
                if (responseLine == null) {
                    connected.set(false);
                    throw new IOException("Server closed connection");
                }
                return NetworkMessage.fromJson(responseLine);
            } catch (IOException e) {
                connected.set(false);
                if (connectWithRetry(maxReconnectAttempts)) {
                    return send(request);
                }
                throw e;
            }
        }
    }

    public NetworkMessage ping() throws IOException {
        return send(NetworkMessage.ping());
    }

    public NetworkMessage login(String username, String password) throws IOException {
        NetworkMessage request = new NetworkMessage(MessageType.LOGIN);
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        request.setPayload(payload);
        return send(request);
    }

    public NetworkMessage saveAnswer(StudentAnswerPayload answer) throws IOException {
        NetworkMessage request = new NetworkMessage(MessageType.SAVE_ANSWER);
        request.setPayload(answer.toJson());
        return send(request);
    }

    public NetworkMessage submitExam(int assignmentId, int totalMarks) throws IOException {
        NetworkMessage request = new NetworkMessage(MessageType.SUBMIT_EXAM);
        JsonObject payload = new JsonObject();
        payload.addProperty("assignmentId", assignmentId);
        payload.addProperty("totalMarks", totalMarks);
        request.setPayload(payload);
        return send(request);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    /**
     * Lightweight payload for saving answers over TCP.
     */
    public static class StudentAnswerPayload {
        private int attemptId;
        private int questionId;
        private Integer selectedOptionId;
        private String shortAnswerText;
        private Boolean correct;
        private int marksObtained;

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("attemptId", attemptId);
            json.addProperty("questionId", questionId);
            if (selectedOptionId != null) {
                json.addProperty("selectedOptionId", selectedOptionId);
            }
            if (shortAnswerText != null) {
                json.addProperty("shortAnswerText", shortAnswerText);
            }
            if (correct != null) {
                json.addProperty("correct", correct);
            }
            json.addProperty("marksObtained", marksObtained);
            return json;
        }

        public void setAttemptId(int attemptId) {
            this.attemptId = attemptId;
        }

        public void setQuestionId(int questionId) {
            this.questionId = questionId;
        }

        public void setSelectedOptionId(Integer selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }

        public void setShortAnswerText(String shortAnswerText) {
            this.shortAnswerText = shortAnswerText;
        }

        public void setCorrect(Boolean correct) {
            this.correct = correct;
        }

        public void setMarksObtained(int marksObtained) {
            this.marksObtained = marksObtained;
        }
    }
}
