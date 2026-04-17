package com.udit.authlib.exception;

public class ErrorResponse {
    private String error;
    private String message;
    private long timestamp;

    public ErrorResponse(String error, String message, long timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
