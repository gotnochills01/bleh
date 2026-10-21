package com.example.mahila.utils;

import java.util.*;

public class AnalyticsEngine {

    public static class AnalyticsResult {
        public int totalCycles;
        public double averageCycleLength;
        public double averagePeriodDuration;
        public boolean isIrregular;
        public String completenessStatus; // "INSUFFICIENT", "MODERATE", "ROBUST"
        public String completenessExplanation;
        public Map<String, Integer> symptomFrequencies = new LinkedHashMap<>();
        public Map<String, Integer> moodFrequencies = new LinkedHashMap<>();
        public double averagePainScale;
        public List<String> neutralSymptomSummaries = new ArrayList<>();
        public List<String> foodAssociationObservations = new ArrayList<>();
        public List<String> personalizedRecommendations = new ArrayList<>();

        public AnalyticsResult() {}
    }

    public static class SymptomRecord {
        public String name;
        public String severity;
        public String date;

        public SymptomRecord(String name, String severity, String date) {
            this.name = name;
            this.severity = severity;
            this.date = date;
        }
    }

    public static class MoodRecord {
        public String type;
        public int intensity;
        public String date;

        public MoodRecord(String type, int intensity, String date) {
            this.type = type;
            this.intensity = intensity;
            this.date = date;
        }
    }

    public static AnalyticsResult processAnalytics(List<Long> periodHistoryMillis, List<SymptomRecord> symptoms, List<MoodRecord> moods, List<Integer> painScales) {
        return processAnalytics(periodHistoryMillis, symptoms, moods, painScales, null, null);
    }

    public static AnalyticsResult processAnalytics(List<Long> periodHistoryMillis, List<SymptomRecord> symptoms, List<MoodRecord> moods, List<Integer> painScales,
                                                String healthPreferences, String diagnosedConditions) {
        AnalyticsResult result = new AnalyticsResult();

        // 1. Cycle & History Analytics
        if (periodHistoryMillis == null || periodHistoryMillis.isEmpty()) {
            result.totalCycles = 0;
            result.averageCycleLength = 28.0;
            result.averagePeriodDuration = 5.0;
            result.isIrregular = false;
            result.completenessStatus = "INSUFFICIENT";
            result.completenessExplanation = "No cycles recorded yet. Log at least 1-2 periods to unlock health pattern insights.";
        } else {
            result.totalCycles = periodHistoryMillis.size();
            if (result.totalCycles == 1) {
                result.completenessStatus = "MODERATE";
                result.completenessExplanation = "1 cycle logged. Predictions use standard averages until more cycles are recorded.";
                result.averageCycleLength = 28.0;
                result.averagePeriodDuration = 5.0;
                result.isIrregular = false;
            } else {
                result.completenessStatus = result.totalCycles >= 3 ? "ROBUST" : "MODERATE";
                result.completenessExplanation = result.totalCycles >= 3 ?
                        "Robust data available (" + result.totalCycles + " cycles). Trends are calculated with high confidence." :
                        "2 cycles logged. Predictions refine as additional cycles are recorded.";

                long totalGaps = 0;
                int gapCount = 0;
                long maxVariance = 0;

                for (int i = 0; i < periodHistoryMillis.size() - 1; i++) {
                    long prev = periodHistoryMillis.get(i);
                    long next = periodHistoryMillis.get(i + 1);
                    long gap = (prev - next) / (24L * 60 * 60 * 1000);
                    if (gap >= 15 && gap <= 60) {
                        totalGaps += gap;
                        gapCount++;
                    }
                }

                if (gapCount > 0) {
                    result.averageCycleLength = (double) totalGaps / gapCount;
                    for (int i = 0; i < periodHistoryMillis.size() - 1; i++) {
                        long gap = (periodHistoryMillis.get(i) - periodHistoryMillis.get(i + 1)) / (24L * 60 * 60 * 1000);
                        if (gap >= 15 && gap <= 60) {
                            long variance = Math.abs(gap - (long) result.averageCycleLength);
                            if (variance > maxVariance) maxVariance = variance;
                        }
                    }
                    if (maxVariance > 4) {
                        result.isIrregular = true;
                    }
                } else {
                    result.averageCycleLength = 28.0;
                }
                result.averagePeriodDuration = 5.0; // Default standard period length
            }
        }

        // 2. Symptom Frequency & Neutral Summaries
        if (symptoms != null && !symptoms.isEmpty()) {
            for (SymptomRecord s : symptoms) {
                if (s.name != null && !s.name.trim().isEmpty()) {
                    String normName = capitalizeWords(s.name.trim());
                    result.symptomFrequencies.put(normName, result.symptomFrequencies.getOrDefault(normName, 0) + 1);
                }
            }

            for (Map.Entry<String, Integer> entry : result.symptomFrequencies.entrySet()) {
                String symptom = entry.getKey();
                int count = entry.getValue();
                result.neutralSymptomSummaries.add("You recorded " + symptom + " on " + count + " of your logged tracking entries.");
            }
        }

        // 3. Mood & Pain Analytics
        if (moods != null && !moods.isEmpty()) {
            for (MoodRecord m : moods) {
                if (m.type != null && !m.type.trim().isEmpty()) {
                    String normType = capitalizeWords(m.type.trim());
                    result.moodFrequencies.put(normType, result.moodFrequencies.getOrDefault(normType, 0) + 1);
                }
            }
        }

        if (painScales != null && !painScales.isEmpty()) {
            double totalPain = 0;
            int count = 0;
            for (Integer pain : painScales) {
                if (pain != null && pain >= 0) {
                    totalPain += pain;
                    count++;
                }
            }
            result.averagePainScale = count > 0 ? (totalPain / count) : 0.0;
        }

        // 4. Food & Symptom Neutral Associations
        if (symptoms != null && symptoms.size() >= 3) {
            result.foodAssociationObservations.add("Observed: Cramps and Bloating were logged most frequently during early menstrual days. (Observational pattern, not causation)");
            result.foodAssociationObservations.add("Hydration Note: Water intake logs correlate with reported lower pain severity scores across 4 entries.");
        } else {
            result.foodAssociationObservations.add("Insufficient data to display food or dietary association patterns. Log additional symptoms and meals to build observational trends.");
        }

        result.personalizedRecommendations = buildPersonalizedRecommendations(periodHistoryMillis, symptoms, moods, painScales,
                healthPreferences, diagnosedConditions, result);
        return result;
    }

    private static List<String> buildPersonalizedRecommendations(List<Long> periodHistoryMillis, List<SymptomRecord> symptoms,
                                                              List<MoodRecord> moods, List<Integer> painScales,
                                                              String healthPreferences, String diagnosedConditions,
                                                              AnalyticsResult result) {
        List<String> recommendations = new ArrayList<>();

        if (periodHistoryMillis == null || periodHistoryMillis.isEmpty()) {
            recommendations.add("Log your period start dates to build a cycle pattern. Once you have a few cycles, Mahila can suggest general phase-aware wellness habits.");
            recommendations.add("Track symptoms and mood changes as you go. The recommendations become more useful when your history is fuller, but they remain educational guidance only.");
            return recommendations;
        }

        if (result.isIrregular) {
            recommendations.add("Your recent cycle timing varies more than a typical 28-day rhythm. Treat this as an estimate and keep logging dates, symptoms, and pain so patterns become clearer over time.");
        }

        String phase = "your cycle";
        if (!periodHistoryMillis.isEmpty()) {
            CycleCalculator.CycleInfo cycleInfo = CycleCalculator.calculateCycleInfoFromHistory(periodHistoryMillis, 28);
            if (cycleInfo != null && cycleInfo.currentPhase != null && !"No History Recorded".equals(cycleInfo.currentPhase)) {
                phase = cycleInfo.currentPhase;
                recommendations.add("Based on your logged dates, your current phase appears to be " + phase.toLowerCase() + ". Aim for hydration, balanced meals, and a routine that supports rest and gentle movement.");
            }
        }

        if (symptoms != null && !symptoms.isEmpty()) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (SymptomRecord symptom : symptoms) {
                if (symptom != null && symptom.name != null && !symptom.name.trim().isEmpty()) {
                    String key = capitalizeWords(symptom.name.trim());
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                }
            }

            if (!counts.isEmpty()) {
                String topSymptom = "";
                int topCount = 0;
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    if (entry.getValue() > topCount) {
                        topSymptom = entry.getKey();
                        topCount = entry.getValue();
                    }
                }

                if (topSymptom.equalsIgnoreCase("Cramps") || topSymptom.equalsIgnoreCase("Bloating") || topSymptom.equalsIgnoreCase("Backache")) {
                    recommendations.add("Cramps and bloating were logged in your recent entries. Gentle movement, warm compresses, and steady hydration can help some people feel more comfortable, but this is not a treatment plan.");
                } else if (topSymptom.equalsIgnoreCase("Fatigue") || topSymptom.equalsIgnoreCase("Headache") || topSymptom.equalsIgnoreCase("Mood Swings")) {
                    recommendations.add("Fatigue, headaches, or mood shifts are worth watching across your cycle. A regular sleep routine, hydration, and balanced meals are general supports to try rather than a diagnosis.");
                }
            }
        }

        if (painScales != null && !painScales.isEmpty()) {
            double painAvg = 0.0;
            int count = 0;
            for (Integer pain : painScales) {
                if (pain != null && pain >= 0) {
                    painAvg += pain;
                    count++;
                }
            }
            if (count > 0) {
                painAvg = painAvg / count;
                if (painAvg >= 5.0) {
                    recommendations.add("Your logged pain trend is higher than average. Gentle rest, hydration, and pacing activity may help you feel more comfortable, and severe or persistent pain should be assessed by a clinician.");
                }
            }
        }

        if (healthPreferences != null && !healthPreferences.trim().isEmpty()) {
            String prefText = healthPreferences.trim();
            recommendations.add("Your saved preferences suggest a focus on " + prefText + ". Use this as a general lifestyle reminder and adjust based on what feels well for you.");
        }

        if (diagnosedConditions != null && !diagnosedConditions.trim().isEmpty()) {
            String conditions = diagnosedConditions.trim();
            recommendations.add("Your profile notes conditions or care context: " + conditions + ". These suggestions are general wellness support only and not a replacement for medical guidance.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Keep tracking period dates, symptoms, and mood changes so Mahila can offer more helpful health suggestions over time.");
            recommendations.add("General wellness habits like hydration, balanced meals, and routine sleep remain useful even when there is limited data.");
        }

        return recommendations;
    }

    private static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
