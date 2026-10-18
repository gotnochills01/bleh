package com.mahila;

import com.mahila.dto.AnalyticsDTO;
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

    @Test
    public void testAnalyticsDto() {
        AnalyticsDTO dto = AnalyticsDTO.builder()
                .averageCycleLength(29.5)
                .averagePeriodDuration(4.8)
                .cycleRegularity("Regular (±2 days)")
                .dataCompletenessStatus("ROBUST")
                .mostCommonSymptom("Cramps")
                .mostCommonMood("Calm")
                .totalCyclesTracked(4)
                .build();

        assertEquals(29.5, dto.getAverageCycleLength(), 0.01);
        assertEquals(4.8, dto.getAveragePeriodDuration(), 0.01);
        assertEquals("Regular (±2 days)", dto.getCycleRegularity());
        assertEquals("ROBUST", dto.getDataCompletenessStatus());
        assertEquals("Cramps", dto.getMostCommonSymptom());
        assertEquals("Calm", dto.getMostCommonMood());
        assertEquals(Integer.valueOf(4), dto.getTotalCyclesTracked());
    }
}
