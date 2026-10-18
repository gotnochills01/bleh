package com.mahila.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_phase", nullable = false, length = 50)
    private String cyclePhase;

    @Column(name = "meal_category", nullable = false, length = 50)
    private String mealCategory;

    @Column(name = "food_item_name", nullable = false, length = 150)
    private String foodItemName;

    @Column(name = "key_nutrients", nullable = false, length = 255)
    private String keyNutrients;

    @Column(name = "health_benefits", columnDefinition = "TEXT")
    private String healthBenefits;

    @Column(name = "dietary_type", nullable = false, length = 30)
    private String dietaryType;

    @Column(name = "budget_level", nullable = false, length = 30)
    private String budgetLevel;

    @Column(name = "evidence_level", nullable = false, length = 50)
    private String evidenceLevel;

    @Column(name = "safety_warnings", columnDefinition = "TEXT")
    private String safetyWarnings;

    @Column(name = "evidence_url", length = 255)
    private String evidenceUrl;

    @Builder.Default
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
