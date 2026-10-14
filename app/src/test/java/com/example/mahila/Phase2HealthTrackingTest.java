package com.example.mahila;

import org.junit.Test;
import static org.junit.Assert.*;

public class Phase2HealthTrackingTest {

    @Test
    public void testDatabaseHelperColumnsAndConstants() {
        assertEquals("diagnosed_conditions", DatabaseHelper.KEY_DIAGNOSED_CONDITIONS);
        assertEquals("health_preferences", DatabaseHelper.KEY_HEALTH_PREFERENCES);
        assertEquals("severity", DatabaseHelper.KEY_SEVERITY);
        assertEquals("moods", DatabaseHelper.TABLE_MOODS);
    }
}
