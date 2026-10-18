package com.mahila.controller;

import com.mahila.dto.NutritionRecommendationDTO;
import com.mahila.model.NutritionRecommendation;
import com.mahila.repository.NutritionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = "*")
public class NutritionController {

    @Autowired
    private NutritionRepository nutritionRepository;

    @GetMapping
    public ResponseEntity<List<NutritionRecommendationDTO>> getRecommendations(
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String dietaryType,
            @RequestParam(required = false) String budgetLevel) {

        List<NutritionRecommendation> items = nutritionRepository.findByFilters(phase, dietaryType, budgetLevel);
        List<NutritionRecommendationDTO> dtos = items.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutritionRecommendationDTO> getById(@PathVariable Long id) {
        return nutritionRepository.findById(id)
                .map(item -> ResponseEntity.ok(mapToDTO(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    private NutritionRecommendationDTO mapToDTO(NutritionRecommendation item) {
        return NutritionRecommendationDTO.builder()
                .id(item.getId())
                .cyclePhase(item.getCyclePhase())
                .mealCategory(item.getMealCategory())
                .foodItemName(item.getFoodItemName())
                .keyNutrients(item.getKeyNutrients())
                .healthBenefits(item.getHealthBenefits())
                .dietaryType(item.getDietaryType())
                .budgetLevel(item.getBudgetLevel())
                .evidenceLevel(item.getEvidenceLevel())
                .safetyWarnings(item.getSafetyWarnings())
                .evidenceUrl(item.getEvidenceUrl())
                .build();
    }
}
