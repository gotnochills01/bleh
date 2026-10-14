package com.mahila;

import com.mahila.dto.AuthRequest;
import com.mahila.dto.MigrationSyncPayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BackendIntegrationTest {

    @Test
    public void testAuthRequestDto() {
        AuthRequest req = AuthRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        assertEquals("user@example.com", req.getEmail());
        assertEquals("password123", req.getPassword());
    }

    @Test
    public void testMigrationSyncPayloadDto() {
        MigrationSyncPayload payload = MigrationSyncPayload.builder()
                .email("user@example.com")
                .age(26)
                .build();

        assertEquals("user@example.com", payload.getEmail());
        assertEquals(26, payload.getAge());
    }
}
