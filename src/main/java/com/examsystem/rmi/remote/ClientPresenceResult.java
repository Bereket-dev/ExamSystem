package com.examsystem.rmi.remote;

import java.io.Serializable;

/**
 * Acknowledgement when a teacher/student registers with the admin RMI server.
 */
public class ClientPresenceResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;

    public ClientPresenceResult() {
    }

    public ClientPresenceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ClientPresenceResult ok(String message) {
        return new ClientPresenceResult(true, message);
    }

    public static ClientPresenceResult fail(String message) {
        return new ClientPresenceResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
