package com.example.mahila;

import com.example.mahila.utils.AnalyticsEngine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AnalyticsEngineTest {

    @Test
    public void testEmptyDataProcessing() {
        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(null, null, null, null);
        assertEquals(0, result.totalCycles);
        assertEquals(28.0, result.averageCycleLength, 0.01);
        assertEquals("INSUFFICIENT", result.completenessStatus);
        assertFalse(result.isIrregular);
        assertTrue(result.symptomFrequencies.isEmpty());
    }

    @Test
    public void testSingleCycleDataProcessing() {
        List<Long> periods = Arrays.asList(System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000));
        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(periods, null, null, null);

        assertEquals(1, result.totalCycles);
        assertEquals("MODERATE", result.completenessStatus);
        assertFalse(result.isIrregular);
    }

    @Test
    public void testRegularCycleProcessing() {
        long now = System.currentTimeMillis();
        long p1 = now;
        long p2 = now - (28L * 24 * 60 * 60 * 1000);
        long p3 = now - (56L * 24 * 60 * 60 * 1000);

        List<Long> periods = Arrays.asList(p1, p2, p3);
        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(periods, null, null, null);

        assertEquals(3, result.totalCycles);
        assertEquals(28.0, result.averageCycleLength, 0.5);
        assertEquals("ROBUST", result.completenessStatus);
        assertFalse(result.isIrregular);
    }

    @Test
    public void testIrregularCycleVarianceDetection() {
        long now = System.currentTimeMillis();
        long p1 = now;
        long p2 = now - (21L * 24 * 60 * 60 * 1000); // 21 day gap
        long p3 = now - (56L * 24 * 60 * 60 * 1000); // 35 day gap

        List<Long> periods = Arrays.asList(p1, p2, p3);
        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(periods, null, null, null);

        assertEquals(3, result.totalCycles);
        assertTrue(result.isIrregular);
    }

    @Test
    public void testSymptomFrequenciesAndNeutralSummaries() {
        List<AnalyticsEngine.SymptomRecord> symptoms = new ArrayList<>();
        symptoms.add(new AnalyticsEngine.SymptomRecord("Cramps", "SEVERE", "2026-10-10"));
        symptoms.add(new AnalyticsEngine.SymptomRecord("Cramps", "MODERATE", "2026-10-11"));
        symptoms.add(new AnalyticsEngine.SymptomRecord("Headache", "LIGHT", "2026-10-12"));

        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(null, symptoms, null, null);

        assertEquals(2, (int) result.symptomFrequencies.get("Cramps"));
        assertEquals(1, (int) result.symptomFrequencies.get("Headache"));
        assertEquals(2, result.neutralSymptomSummaries.size());
        assertTrue(result.neutralSymptomSummaries.get(0).contains("recorded Cramps on 2 of your logged tracking entries"));
    }

    @Test
    public void testPersonalizedRecommendationsUseRealCycleAndSymptomData() {
        long now = System.currentTimeMillis();
        List<Long> periods = Arrays.asList(
                now,
                now - (28L * 24 * 60 * 60 * 1000),
                now - (56L * 24 * 60 * 60 * 1000)
        );
        List<AnalyticsEngine.SymptomRecord> symptoms = new ArrayList<>();
        symptoms.add(new AnalyticsEngine.SymptomRecord("Cramps", "SEVERE", "2026-10-10"));
        symptoms.add(new AnalyticsEngine.SymptomRecord("Bloating", "MODERATE", "2026-10-11"));

        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(
                periods,
                symptoms,
                null,
                Arrays.asList(5, 6),
                "balanced meals; hydration",
                "No diagnosed conditions"
        );

        assertFalse(result.personalizedRecommendations.isEmpty());
        assertTrue(result.personalizedRecommendations.stream().anyMatch(item -> item.toLowerCase().contains("cycle") || item.toLowerCase().contains("hydration") || item.toLowerCase().contains("cramps")));
        assertTrue(result.personalizedRecommendations.stream().anyMatch(item -> item.toLowerCase().contains("balanced") || item.toLowerCase().contains("preferences")));
    }

    @Test
    public void testMissingDataProducesCautiousRecommendations() {
        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(null, null, null, null);

        assertEquals("INSUFFICIENT", result.completenessStatus);
        assertFalse(result.personalizedRecommendations.isEmpty());
        assertTrue(result.personalizedRecommendations.get(0).toLowerCase().contains("log your period start dates"));
        assertFalse(result.personalizedRecommendations.get(0).contains("certain"));
    }

    @Test
    public void testIrregularCyclesGenerateEstimateWarnings() {
        long now = System.currentTimeMillis();
        List<Long> periods = Arrays.asList(
                now,
                now - (21L * 24 * 60 * 60 * 1000),
                now - (56L * 24 * 60 * 60 * 1000)
        );

        AnalyticsEngine.AnalyticsResult result = AnalyticsEngine.processAnalytics(periods, null, null, null);

        assertTrue(result.isIrregular);
        assertTrue(result.personalizedRecommendations.stream().anyMatch(item -> item.toLowerCase().contains("estimate") || item.toLowerCase().contains("varies more than a typical 28-day rhythm")));
    }
}
