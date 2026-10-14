package com.example.mahila;

import com.example.mahila.utils.CycleCalculator;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class CycleCalculatorTest {

    @Test
    public void testEmptyHistoryReturnsInsufficientDataState() {
        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(null, 28);
        assertNotNull(info);
        assertEquals(0, info.cycleDay);
        assertEquals("No History Recorded", info.currentPhase);
        assertEquals("Not Available", info.nextPeriodDate);
        assertTrue(info.isEstimated);
        assertFalse(info.isIrregular);
    }

    @Test
    public void testSinglePeriodReturnsEstimatedCycleInfo() {
        long now = System.currentTimeMillis();
        long tenDaysAgo = now - (10L * 24 * 60 * 60 * 1000);
        List<Long> history = new ArrayList<>();
        history.add(tenDaysAgo);

        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(history, 28);
        assertNotNull(info);
        assertEquals(11, info.cycleDay);
        assertEquals("Follicular Phase", info.currentPhase);
        assertTrue(info.isEstimated);
        assertFalse(info.isIrregular);
    }

    @Test
    public void testIrregularCycleVarianceDetection() {
        long now = System.currentTimeMillis();
        List<Long> history = new ArrayList<>();
        history.add(now - (10L * 24 * 60 * 60 * 1000));
        history.add(now - (30L * 24 * 60 * 60 * 1000)); // gap 20
        history.add(now - (70L * 24 * 60 * 60 * 1000)); // gap 40 (high variance)

        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(history, 28);
        assertNotNull(info);
        assertTrue(info.isIrregular);
    }
}
