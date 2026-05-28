package com.examsystem.rmi;

import com.examsystem.model.StudentAnswer;
import com.examsystem.rmi.client.RMIClient;
import com.examsystem.rmi.remote.ClientPresenceResult;
import com.examsystem.rmi.remote.LoginResult;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.rmi.remote.RemoteAnswerPayload;
import com.examsystem.rmi.remote.SyncBundle;
import com.examsystem.rmi.remote.SyncResult;
import com.examsystem.rmi.server.RMIServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * Central lifecycle manager for RMI server and client.
 */
public class RMIManager {
    private static final Logger logger = LoggerFactory.getLogger(RMIManager.class);
    private static RMIManager instance;

    private RMIServer rmiServer;
    private RMIClient rmiClient;

    private RMIManager() {
    }

    public static synchronized RMIManager getInstance() {
        if (instance == null) {
            instance = new RMIManager();
        }
        return instance;
    }

    public synchronized void startServer() {
        try {
            if (rmiServer == null) {
                rmiServer = new RMIServer();
            }
            if (!rmiServer.isRunning()) {
                rmiServer.start();
            }
        } catch (RemoteException e) {
            logger.error("Failed to start RMIServer", e);
        }
    }

    public synchronized void stopServer() {
        if (rmiServer != null) {
            rmiServer.stop();
        }
    }

    public boolean isServerRunning() {
        return rmiServer != null && rmiServer.isRunning();
    }

    public synchronized boolean connectClient() {
        if (rmiClient == null) {
            rmiClient = new RMIClient();
        }
        return rmiClient.connect();
    }

    public synchronized boolean connectClient(String host, int port) {
        if (rmiClient == null) {
            rmiClient = new RMIClient(host, port);
        } else {
            rmiClient.configure(host, port);
        }
        return rmiClient.connect();
    }

    /**
     * Tests reachability of an admin RMI registry without affecting the active client session.
     */
    public boolean testConnection(String host, int port) {
        RMIClient probe = new RMIClient(host, port);
        return probe.connect();
    }

    public synchronized void disconnectClient() {
        if (rmiClient != null) {
            rmiClient.disconnect();
        }
    }

    public boolean isClientConnected() {
        return rmiClient != null && rmiClient.isConnected();
    }

    public String ping() throws RemoteException {
        if (!connectClient()) {
            throw new RemoteException("RMI server unavailable");
        }
        return rmiClient.ping();
    }

    public LoginResult clientLogin(String username, String password) {
        try {
            if (!connectClient()) {
                return LoginResult.failure("RMI server unavailable");
            }
            return rmiClient.login(username, password);
        } catch (RemoteException e) {
            logger.warn("RMI login failed: {}", e.getMessage());
            return LoginResult.failure(e.getMessage());
        }
    }

    /**
     * Notifies the admin server that this client completed online connection setup.
     */
    public ClientPresenceResult registerClientPresence(String username, String role) {
        try {
            if (!isClientConnected() && !connectClient()) {
                return ClientPresenceResult.fail("Not connected to admin server");
            }
            return rmiClient.registerClientPresence(username, role);
        } catch (RemoteException e) {
            logger.warn("RMI client presence failed: {}", e.getMessage());
            return ClientPresenceResult.fail(e.getMessage());
        }
    }

    public void syncSaveAnswer(StudentAnswer answer) {
        if (!isClientConnected()) {
            return;
        }
        try {
            RemoteAnswerPayload payload = new RemoteAnswerPayload();
            payload.setAttemptId(answer.getAttemptId());
            payload.setQuestionId(answer.getQuestionId());
            payload.setSelectedOptionId(answer.getSelectedOptionId());
            payload.setShortAnswerText(answer.getShortAnswerText());
            payload.setCorrect(answer.isCorrect());
            payload.setMarksObtained(answer.getMarksObtained());
            rmiClient.saveAnswer(payload);
        } catch (RemoteException e) {
            logger.warn("RMI save failed, local save retained: {}", e.getMessage());
        }
    }

    public void syncSubmitExam(int assignmentId, int totalMarks) {
        if (!isClientConnected()) {
            return;
        }
        try {
            rmiClient.submitExam(assignmentId, totalMarks);
        } catch (RemoteException e) {
            logger.warn("RMI submit failed, local submit retained: {}", e.getMessage());
        }
    }

    public MonitoringSummary getMonitoringSummary(int teacherId) {
        try {
            if (!connectClient()) {
                return new MonitoringSummary(0, 0);
            }
            return rmiClient.getMonitoringSummary(teacherId);
        } catch (RemoteException e) {
            logger.warn("RMI monitoring fetch failed: {}", e.getMessage());
            return new MonitoringSummary(0, 0);
        }
    }

    public SyncBundle pullSyncBundle() throws RemoteException {
        if (!connectClient()) {
            throw new RemoteException("RMI server unavailable");
        }
        return rmiClient.pullSyncBundle();
    }

    public SyncResult pushSyncBundle(SyncBundle bundle) throws RemoteException {
        if (!connectClient()) {
            return SyncResult.fail("RMI server unavailable");
        }
        return rmiClient.pushSyncBundle(bundle);
    }

    public synchronized void shutdown() {
        stopServer();
        disconnectClient();
    }
}
