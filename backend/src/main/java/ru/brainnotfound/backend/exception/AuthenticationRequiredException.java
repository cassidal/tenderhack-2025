package ru.brainnotfound.backend.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationRequiredException extends AuthenticationException {
    public AuthenticationRequiredException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AuthenticationRequiredException(String msg) {
        super(msg);
    }

    public AuthenticationRequiredException() {
        super("Authentication required");
    }
}
