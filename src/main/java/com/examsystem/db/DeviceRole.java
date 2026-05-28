package com.examsystem.db;

/**
 * Device role: admin hosts the central database; client devices use a local backup DB.
 */
public enum DeviceRole {
    ADMIN,
    CLIENT;

    public static DeviceRole fromConfig(String value) {
        if (value == null) {
            return ADMIN;
        }
        return "client".equalsIgnoreCase(value.trim()) ? CLIENT : ADMIN;
    }
}
