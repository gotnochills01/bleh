package com.mahila.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        log.warn("Handled business runtime exception: {}", msg);

        Map<String, Object> body = new HashMap<>();
        body.put("error", true);
        body.put("message", msg);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.contains("Invalid password") || msg.contains("User not found")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("already registered") || msg.contains("already exists")) {
            status = HttpStatus.CONFLICT;
        }

        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled internal server exception", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", true);
        body.put("message", "An unexpected server error occurred. Please try again later.");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
