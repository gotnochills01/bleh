package com.mahila.repository;

import com.mahila.model.NutritionRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionRepository extends JpaRepository<NutritionRecommendation, Long> {

    @Query("SELECT n FROM NutritionRecommendation n WHERE " +
           "(:phase IS NULL OR UPPER(n.cyclePhase) = UPPER(:phase) OR UPPER(n.cyclePhase) = 'GENERAL') AND " +
           "(:dietaryType IS NULL OR UPPER(:dietaryType) = 'ALL' OR UPPER(n.dietaryType) = UPPER(:dietaryType) OR UPPER(n.dietaryType) = 'ALL') AND " +
           "(:budgetLevel IS NULL OR UPPER(:budgetLevel) = 'ALL' OR UPPER(n.budgetLevel) = UPPER(:budgetLevel))")
    List<NutritionRecommendation> findByFilters(
            @Param("phase") String phase,
            @Param("dietaryType") String dietaryType,
            @Param("budgetLevel") String budgetLevel
    );
}
