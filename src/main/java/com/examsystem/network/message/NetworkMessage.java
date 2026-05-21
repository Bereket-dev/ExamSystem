package com.examsystem.network.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * JSON-serializable TCP message envelope.
 */
public class NetworkMessage {
    private static final Gson GSON = new Gson();

    private MessageType type;
    private String requestId;
    private boolean success = true;
    private String errorMessage;
    private JsonObject payload;

    public NetworkMessage() {
    }

    public NetworkMessage(MessageType type) {
        this.type = type;
    }

    public static NetworkMessage ping() {
        return new NetworkMessage(MessageType.PING);
    }

    public static NetworkMessage pong(String requestId) {
        NetworkMessage message = new NetworkMessage(MessageType.PONG);
        message.setRequestId(requestId);
        return message;
    }

    public static NetworkMessage error(String requestId, String error) {
        NetworkMessage message = new NetworkMessage(MessageType.ERROR);
        message.setRequestId(requestId);
        message.setSuccess(false);
        message.setErrorMessage(error);
        return message;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static NetworkMessage fromJson(String json) {
        return GSON.fromJson(json, NetworkMessage.class);
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }
}
