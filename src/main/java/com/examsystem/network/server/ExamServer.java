package com.examsystem.network.server;

import com.examsystem.util.ConfigManager;
import com.examsystem.util.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP server for multi-client exam system communication.
 * See context/CLASS_DIAGRAM_REFERENCE.md - ExamServer.
 */
public class ExamServer {
    private static final Logger logger = LoggerFactory.getLogger(ExamServer.class);

    private final int port;
    private final int maxClients;
    private final NetworkRequestHandler requestHandler = new NetworkRequestHandler();
    private final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private Thread acceptThread;

    public ExamServer() {
        this.port = ConfigManager.getIntProperty("network.server.port", 5000);
        this.maxClients = ConfigManager.getIntProperty("network.server.max.clients", 100);
    }

    public ExamServer(int port, int maxClients) {
        this.port = port;
        this.maxClients = maxClients;
    }

    public synchronized void start() {
        if (running.get()) {
            logger.info("ExamServer already running on port {}", port);
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            clientExecutor = ThreadPoolManager.getInstance().getClientExecutor();
            running.set(true);

            acceptThread = new Thread(this::acceptLoop, "ExamServer-Accept");
            acceptThread.setDaemon(true);
            acceptThread.start();

            logger.info("ExamServer started on port {} (max clients: {})", port, maxClients);
        } catch (IOException e) {
            running.set(false);
            logger.error("Failed to start ExamServer on port {}", port, e);
            throw new RuntimeException("Cannot start TCP server", e);
        }
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                if (activeClients.size() >= maxClients) {
                    logger.warn("Max clients ({}) reached, rejecting new connection", maxClients);
                    Socket rejected = serverSocket.accept();
                    rejected.close();
                    continue;
                }

                Socket clientSocket = serverSocket.accept();
                String clientId = "client-" + clientCounter.incrementAndGet();

                ClientHandler handler = new ClientHandler(clientSocket, clientId, requestHandler,
                        () -> activeClients.remove(clientId));

                activeClients.put(clientId, handler);
                clientExecutor.submit(handler);
                logger.info("Client connected: {} (active: {})", clientId, activeClients.size());
            } catch (IOException e) {
                if (running.get()) {
                    logger.error("Error accepting client connection", e);
                }
            }
        }
    }

    public synchronized void stop() {
        if (!running.get()) {
            return;
        }
        running.set(false);

        for (ClientHandler handler : activeClients.values()) {
            handler.shutdown();
        }
        activeClients.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing server socket", e);
            }
        }

        if (acceptThread != null) {
            acceptThread.interrupt();
        }

        logger.info("ExamServer stopped");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }

    public int getActiveClientCount() {
        return activeClients.size();
    }
}
