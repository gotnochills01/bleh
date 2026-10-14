package com.mahila.service;

import com.mahila.dto.AnalyticsDTO;
import com.mahila.model.Mood;
import com.mahila.model.Period;
import com.mahila.model.Symptom;
import com.mahila.model.User;
import com.mahila.repository.MoodRepository;
import com.mahila.repository.PeriodRepository;
import com.mahila.repository.SymptomRepository;
import com.mahila.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private UserRepository userRepository;

    public AnalyticsDTO getAnalytics(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Period> periods = periodRepository.findByUserIdOrderByStartDateDesc(user.getId());
        List<Symptom> symptoms = symptomRepository.findByUserIdOrderByLoggedDateDesc(user.getId());
        List<Mood> moods = moodRepository.findByUserIdOrderByLoggedDateDesc(user.getId());

        double avgCycleLength = 28.0;
        String regularity = "Regular (±2 days)";

        if (periods.size() >= 2) {
            List<Long> gaps = new ArrayList<>();
            for (int i = 0; i < periods.size() - 1; i++) {
                long days = ChronoUnit.DAYS.between(periods.get(i + 1).getStartDate(), periods.get(i).getStartDate());
                if (days > 15 && days < 60) {
                    gaps.add(days);
                }
            }
            if (!gaps.isEmpty()) {
                double computedAvg = gaps.stream().mapToLong(Long::longValue).average().orElse(28.0);
                avgCycleLength = computedAvg;
                long variance = gaps.stream().mapToLong(g -> Math.abs(g - (long) computedAvg)).max().orElse(0L);
                if (variance > 4) {
                    regularity = "Irregular (High Variance)";
                }
            }
        }

        Map<String, Integer> symptomFreq = new HashMap<>();
        for (Symptom s : symptoms) {
            symptomFreq.put(s.getSymptomName(), symptomFreq.getOrDefault(s.getSymptomName(), 0) + 1);
        }
        String topSymptom = symptomFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None logged");

        Map<String, Integer> moodFreq = new HashMap<>();
        for (Mood m : moods) {
            moodFreq.put(m.getMoodType(), moodFreq.getOrDefault(m.getMoodType(), 0) + 1);
        }
        String topMood = moodFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None logged");

        return AnalyticsDTO.builder()
                .averageCycleLength(avgCycleLength)
                .cycleRegularity(regularity)
                .mostCommonSymptom(topSymptom)
                .mostCommonMood(topMood)
                .totalCyclesTracked(periods.size())
                .symptomFrequencies(symptomFreq)
                .moodFrequencies(moodFreq)
                .build();
    }
}
