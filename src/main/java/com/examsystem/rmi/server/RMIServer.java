package com.examsystem.rmi.server;

import com.examsystem.rmi.remote.ExamRemoteService;
import com.examsystem.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI registry and remote service host.
 * See context/CLASS_DIAGRAM_REFERENCE.md - RMIServer.
 */
public class RMIServer {
    private static final Logger logger = LoggerFactory.getLogger(RMIServer.class);

    private final String host;
    private final int port;
    private final String serviceName;

    private Registry registry;
    private ExamRemoteServiceImpl remoteService;
    private boolean running;

    public RMIServer() {
        this.host = ConfigManager.getProperty("rmi.registry.host", "localhost");
        this.port = ConfigManager.getIntProperty("rmi.registry.port", 1099);
        this.serviceName = ConfigManager.getProperty("rmi.service.name", "ExamRemoteService");
    }

    public synchronized void start() throws RemoteException {
        if (running) {
            logger.info("RMIServer already running on port {}", port);
            return;
        }

        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(host, port);
            logger.info("Using existing RMI registry on port {}", port);
        }

        remoteService = new ExamRemoteServiceImpl();
        registry.rebind(serviceName, remoteService);

        running = true;
        logger.info("RMIServer started: rmi://{}:{}/{}", host, port, serviceName);
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        try {
            if (registry != null) {
                registry.unbind(serviceName);
            }
            if (remoteService != null) {
                UnicastRemoteObject.unexportObject(remoteService, true);
            }
        } catch (Exception e) {
            logger.warn("Error stopping RMIServer: {}", e.getMessage());
        } finally {
            running = false;
            remoteService = null;
            logger.info("RMIServer stopped");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public String getServiceName() {
        return serviceName;
    }
}
