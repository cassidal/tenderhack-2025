package ru.brainnotfound.backend.exception;

public class SessionException extends RuntimeException {
    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionException(String message) {
        super(message);
    }
}
