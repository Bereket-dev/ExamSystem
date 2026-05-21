package com.examsystem.network.message;

/**
 * TCP message types for ExamSystem distributed communication.
 */
public enum MessageType {
    PING,
    PONG,
    LOGIN,
    LOGIN_RESPONSE,
    SAVE_ANSWER,
    SUBMIT_EXAM,
    GET_ASSIGNED_EXAMS,
    GET_MONITORING,
    ERROR,
    ACK
}
