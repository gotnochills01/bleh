package com.example.mahila.dto;

public class NutritionDTO {
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

    public NutritionDTO() {}

    public NutritionDTO(Long id, String cyclePhase, String mealCategory, String foodItemName, String keyNutrients, String healthBenefits, String dietaryType, String budgetLevel, String evidenceLevel, String safetyWarnings, String evidenceUrl) {
        this.id = id;
        this.cyclePhase = cyclePhase;
        this.mealCategory = mealCategory;
        this.foodItemName = foodItemName;
        this.keyNutrients = keyNutrients;
        this.healthBenefits = healthBenefits;
        this.dietaryType = dietaryType;
        this.budgetLevel = budgetLevel;
        this.evidenceLevel = evidenceLevel;
        this.safetyWarnings = safetyWarnings;
        this.evidenceUrl = evidenceUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCyclePhase() { return cyclePhase; }
    public void setCyclePhase(String cyclePhase) { this.cyclePhase = cyclePhase; }

    public String getMealCategory() { return mealCategory; }
    public void setMealCategory(String mealCategory) { this.mealCategory = mealCategory; }

    public String getFoodItemName() { return foodItemName; }
    public void setFoodItemName(String foodItemName) { this.foodItemName = foodItemName; }

    public String getKeyNutrients() { return keyNutrients; }
    public void setKeyNutrients(String keyNutrients) { this.keyNutrients = keyNutrients; }

    public String getHealthBenefits() { return healthBenefits; }
    public void setHealthBenefits(String healthBenefits) { this.healthBenefits = healthBenefits; }

    public String getDietaryType() { return dietaryType; }
    public void setDietaryType(String dietaryType) { this.dietaryType = dietaryType; }

    public String getBudgetLevel() { return budgetLevel; }
    public void setBudgetLevel(String budgetLevel) { this.budgetLevel = budgetLevel; }

    public String getEvidenceLevel() { return evidenceLevel; }
    public void setEvidenceLevel(String evidenceLevel) { this.evidenceLevel = evidenceLevel; }

    public String getSafetyWarnings() { return safetyWarnings; }
    public void setSafetyWarnings(String safetyWarnings) { this.safetyWarnings = safetyWarnings; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
}
