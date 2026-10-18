package com.mahila.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionRecommendationDTO {
    private Long id;
    private String cyclePhase;
    private String mealCategory;
    private String foodItemName;
    private String keyNutrients;
    private String healthBenefits;
    private String dietaryType;
    private String budgetLevel;
    private String evidenceLevel;
    private String safetyWarnings;
    private String evidenceUrl;
}
