package com.examsystem.network.server;

import com.examsystem.network.message.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles a single TCP client connection (one thread per client).
 * See context/THREADING_REFERENCE.md - TCP Client Handler.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket clientSocket;
    private final String clientId;
    private final NetworkRequestHandler requestHandler;
    private final Runnable onDisconnect;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ClientHandler(Socket clientSocket, String clientId, NetworkRequestHandler requestHandler,
            Runnable onDisconnect) {
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.requestHandler = requestHandler;
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void run() {
        logger.info("Client handler started for {}", clientId);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            while (running.get() && !clientSocket.isClosed()) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.isBlank()) {
                    continue;
                }

                NetworkMessage request = NetworkMessage.fromJson(line);
                NetworkMessage response = requestHandler.handle(request);
                writer.println(response.toJson());
            }
        } catch (IOException e) {
            logger.warn("Client {} disconnected: {}", clientId, e.getMessage());
        } finally {
            close();
            onDisconnect.run();
            logger.info("Client handler stopped for {}", clientId);
        }
    }

    public void shutdown() {
        running.set(false);
        close();
    }

    private void close() {
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing client socket {}", clientId, e);
        }
    }
}
