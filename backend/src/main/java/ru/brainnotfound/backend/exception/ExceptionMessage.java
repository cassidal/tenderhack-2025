package ru.brainnotfound.backend.exception;

import lombok.*;

import java.util.*;

@Getter
public class ExceptionMessage {
    private int status;
    private String message;
    private Date timestamp;

    private ExceptionMessage() {
    }

    public static ExceptionMessageBuilder builder() {
        return new ExceptionMessageBuilder();
    }

    public static class ExceptionMessageBuilder {
        private final ExceptionMessage exceptionMessage;

        private ExceptionMessageBuilder() {
            this.exceptionMessage = new ExceptionMessage();
        }

        public ExceptionMessageBuilder status(int status) {
            exceptionMessage.status = status;
            return this;
        }

        public ExceptionMessageBuilder message(String message) {
            exceptionMessage.message = message;
            return this;
        }

        public ExceptionMessageBuilder timestamp(Date timestamp) {
            exceptionMessage.timestamp = timestamp;
            return this;
        }

        public ExceptionMessage build() {
            return exceptionMessage;
        }
    }
}
