package ru.brainnotfound.backend.controller;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.security.web.access.*;
import org.springframework.web.bind.annotation.*;
import ru.brainnotfound.backend.exception.*;

import java.io.*;
import java.util.*;

@RestControllerAdvice
public class ExceptionController implements AccessDeniedHandler, AuthenticationEntryPoint {
    protected final ObjectMapper objectMapper;

    public ExceptionController(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        ExceptionMessage message = ExceptionMessage.builder().message("Authentication required").status(HttpServletResponse.SC_UNAUTHORIZED).timestamp(new Date()).build();

        writeError(response, message, e);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        ExceptionMessage message = ExceptionMessage.builder().message("Access denied").status(HttpServletResponse.SC_FORBIDDEN).timestamp(new Date()).build();

        writeError(response, message, e);
    }

    private void writeError(HttpServletResponse response, ExceptionMessage message, Exception e) throws IOException {
        response.setStatus(message.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(message));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionMessage> handleException(ValidationException e) {
        var message = ExceptionMessage.builder().message(e.getMessage()).status(HttpStatus.BAD_REQUEST.value()).timestamp(new Date()).build();

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionMessage> handleException(AuthenticationException e) {
        var message = ExceptionMessage.builder().message(e.getMessage()).status(HttpStatus.UNAUTHORIZED.value()).timestamp(new Date()).build();

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionMessage> handleException(SessionException e) {
        var message = ExceptionMessage.builder().message(e.getMessage()).status(HttpStatus.BAD_REQUEST.value()).timestamp(new Date()).build();

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    public void badRequest(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        var message = ExceptionMessage.builder().message(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).timestamp(new Date()).build();

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
