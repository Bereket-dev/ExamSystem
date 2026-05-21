package com.examsystem.rmi.client;

import com.examsystem.rmi.remote.ExamRemoteService;
import com.examsystem.rmi.remote.LoginResult;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.rmi.remote.RemoteAnswerPayload;
import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI client with registry lookup and retry logic for slow LAN.
 * See context/KNOWN_ISSUES.md - RMI timeout retry.
 */
public class RMIClient {
    private static final Logger logger = LoggerFactory.getLogger(RMIClient.class);

    private final String host;
    private final int port;
    private final String serviceName;
    private final int maxRetries;
    private final long retryDelayMs;

    private ExamRemoteService remoteService;
    private boolean connected;

    public RMIClient() {
        this.host = ConfigManager.getProperty("rmi.registry.host", "localhost");
        this.port = ConfigManager.getIntProperty("rmi.registry.port", 1099);
        this.serviceName = ConfigManager.getProperty("rmi.service.name", "ExamRemoteService");
        this.maxRetries = ConfigManager.getIntProperty("rmi.connect.retry.max", 3);
        this.retryDelayMs = ConfigManager.getIntProperty("rmi.connect.retry.delay.ms", 2000);
    }

    public boolean connect() {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, port);
                remoteService = (ExamRemoteService) registry.lookup(serviceName);
                String pong = remoteService.ping();
                connected = "PONG".equals(pong);
                if (connected) {
                    logger.info("RMIClient connected to rmi://{}:{}/{}", host, port, serviceName);
                    return true;
                }
            } catch (RemoteException | NotBoundException e) {
                logger.warn("RMI connect attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                connected = false;
                remoteService = null;
                sleep(retryDelayMs);
            }
        }
        return false;
    }

    public void disconnect() {
        connected = false;
        remoteService = null;
    }

    public boolean isConnected() {
        return connected && remoteService != null;
    }

    public String ping() throws RemoteException {
        ensureConnected();
        return remoteService.ping();
    }

    public LoginResult login(String username, String password) throws RemoteException {
        ensureConnected();
        return remoteService.login(username, password);
    }

    public boolean saveAnswer(RemoteAnswerPayload payload) throws RemoteException {
        ensureConnected();
        return remoteService.saveAnswer(payload);
    }

    public boolean submitExam(int assignmentId, int totalMarks) throws RemoteException {
        ensureConnected();
        return remoteService.submitExam(assignmentId, totalMarks);
    }

    public MonitoringSummary getMonitoringSummary(int teacherId) throws RemoteException {
        ensureConnected();
        return remoteService.getMonitoringSummary(teacherId);
    }

    private void ensureConnected() throws RemoteException {
        if (!isConnected() && !connect()) {
            throw new RemoteException("Not connected to RMI registry at " + host + ":" + port);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
