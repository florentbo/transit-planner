package com.transit.client;

public class StibApiException extends RuntimeException {

    public StibApiException(String message) {
        super(message);
    }

    public StibApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
