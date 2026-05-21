package com.examsystem.rmi.remote;

import java.io.Serializable;

/**
 * Serializable login response for RMI transport.
 */
public class LoginResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String errorMessage;
    private int userId;
    private String username;
    private String fullName;
    private String role;

    public static LoginResult success(int userId, String username, String fullName, String role) {
        LoginResult result = new LoginResult();
        result.success = true;
        result.userId = userId;
        result.username = username;
        result.fullName = fullName;
        result.role = role;
        return result;
    }

    public static LoginResult failure(String errorMessage) {
        LoginResult result = new LoginResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
