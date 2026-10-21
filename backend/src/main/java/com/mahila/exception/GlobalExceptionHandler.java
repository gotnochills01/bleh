package com.mahila.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();

        if ("Email already registered!".equals(message)
                || "Invalid password!".equals(message)
                || (message != null && message.startsWith("User not found with email:"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", true,
                    "message", "Email already registered!".equals(message)
                            ? "Email already registered."
                            : "Invalid credentials.",
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }

        log.error("Unexpected runtime exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", true,
                "message", "An unexpected server error occurred.",
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unexpected server exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", true,
                "message", "An unexpected server error occurred.",
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }
}
