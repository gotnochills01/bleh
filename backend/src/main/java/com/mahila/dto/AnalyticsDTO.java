package com.mahila.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDTO {
    private Double averageCycleLength;
    private Double averagePeriodDuration;
    private String cycleRegularity; // "Regular (±2 days)", "Irregular"
    private String dataCompletenessStatus; // "INSUFFICIENT", "MODERATE", "ROBUST"
    private String mostCommonSymptom;
    private String mostCommonMood;
    private Integer totalCyclesTracked;
    private List<Integer> cycleLengthHistory;
    private Map<String, Integer> moodFrequencies;
    private Map<String, Integer> symptomFrequencies;
}
