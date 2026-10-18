package com.example.mahila;

import com.example.mahila.dto.NutritionDTO;
import org.junit.Test;

import static org.junit.Assert.*;

public class NutritionTest {

    @Test
    public void testNutritionDTOGettersAndSetters() {
        NutritionDTO dto = new NutritionDTO();
        dto.setId(10L);
        dto.setCyclePhase("MENSTRUAL");
        dto.setMealCategory("Main Meal");
        dto.setFoodItemName("Spinach & Lentil Dal");
        dto.setKeyNutrients("Non-heme Iron, Vitamin C");
        dto.setHealthBenefits("Supports red blood cell production");
        dto.setDietaryType("VEGAN");
        dto.setBudgetLevel("BUDGET");
        dto.setEvidenceLevel("WHO Guidelines");
        dto.setSafetyWarnings("Monitor Vitamin K if taking anticoagulants");
        dto.setEvidenceUrl("https://pubmed.ncbi.nlm.nih.gov/20200263/");

        assertEquals(Long.valueOf(10L), dto.getId());
        assertEquals("MENSTRUAL", dto.getCyclePhase());
        assertEquals("Main Meal", dto.getMealCategory());
        assertEquals("Spinach & Lentil Dal", dto.getFoodItemName());
        assertEquals("Non-heme Iron, Vitamin C", dto.getKeyNutrients());
        assertEquals("Supports red blood cell production", dto.getHealthBenefits());
        assertEquals("VEGAN", dto.getDietaryType());
        assertEquals("BUDGET", dto.getBudgetLevel());
        assertEquals("WHO Guidelines", dto.getEvidenceLevel());
        assertEquals("Monitor Vitamin K if taking anticoagulants", dto.getSafetyWarnings());
        assertEquals("https://pubmed.ncbi.nlm.nih.gov/20200263/", dto.getEvidenceUrl());
    }

    @Test
    public void testEvidenceUrlValidation() {
        NutritionDTO validDto = new NutritionDTO(1L, "LUTEAL", "Snack", "Greek Yogurt", "Calcium", "Eases PMS", "VEGETARIAN", "MODERATE", "RCT", null, "https://pubmed.ncbi.nlm.nih.gov/28215622/");
        assertTrue(validDto.getEvidenceUrl().startsWith("http"));

        NutritionDTO invalidDto = new NutritionDTO(2L, "GENERAL", "Tea", "Ginger Tea", "Gingerols", "Eases nausea", "VEGAN", "BUDGET", "Review", null, "invalid-link");
        assertFalse(invalidDto.getEvidenceUrl().startsWith("http://") || invalidDto.getEvidenceUrl().startsWith("https://"));
    }
}
