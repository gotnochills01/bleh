package com.mahila;

import com.mahila.dto.AuthRequest;
import com.mahila.dto.MigrationSyncPayload;
import com.mahila.dto.NutritionRecommendationDTO;
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

    @Test
    public void testNutritionRecommendationDto() {
        NutritionRecommendationDTO dto = NutritionRecommendationDTO.builder()
                .id(1L)
                .cyclePhase("MENSTRUAL")
                .foodItemName("Spinach & Lentil Dal")
                .keyNutrients("Iron, Vitamin C")
                .dietaryType("VEGAN")
                .budgetLevel("BUDGET")
                .evidenceLevel("WHO Dietary Guidelines")
                .evidenceUrl("https://pubmed.ncbi.nlm.nih.gov/20200263/")
                .build();

        assertEquals("MENSTRUAL", dto.getCyclePhase());
        assertEquals("Spinach & Lentil Dal", dto.getFoodItemName());
        assertEquals("Iron, Vitamin C", dto.getKeyNutrients());
        assertEquals("VEGAN", dto.getDietaryType());
        assertEquals("BUDGET", dto.getBudgetLevel());
        assertEquals("WHO Dietary Guidelines", dto.getEvidenceLevel());
        assertEquals("https://pubmed.ncbi.nlm.nih.gov/20200263/", dto.getEvidenceUrl());
    }
}
