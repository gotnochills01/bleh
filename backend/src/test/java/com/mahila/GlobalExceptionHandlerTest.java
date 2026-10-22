package com.mahila;

import com.mahila.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testInvalidPasswordReturnsUnauthorized() {
        RuntimeException ex = new RuntimeException("Invalid password!");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("error"));
        assertEquals("Invalid password!", response.getBody().get("message"));
        assertEquals(401, response.getBody().get("status"));
    }

    @Test
    public void testDuplicateRegistrationReturnsConflict() {
        RuntimeException ex = new RuntimeException("Email already registered!");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("error"));
        assertEquals("Email already registered!", response.getBody().get("message"));
        assertEquals(409, response.getBody().get("status"));
    }

    @Test
    public void testUnexpectedExceptionHidesInternalDetails() {
        Exception ex = new NullPointerException("Internal null reference error at line 142");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("error"));
        assertEquals("An unexpected server error occurred. Please try again later.", response.getBody().get("message"));
        assertFalse(((String) response.getBody().get("message")).contains("NullPointerException"));
        assertEquals(500, response.getBody().get("status"));
    }
}
