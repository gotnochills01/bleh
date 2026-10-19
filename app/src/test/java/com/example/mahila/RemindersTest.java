package com.example.mahila;

import org.junit.Test;
import static org.junit.Assert.*;

public class RemindersTest {

    @Test
    public void testRemindersTableAndKeyConstants() {
        assertEquals("reminders", DatabaseHelper.TABLE_REMINDERS);
        assertEquals("title", DatabaseHelper.KEY_REMINDER_TITLE);
        assertEquals("category", DatabaseHelper.KEY_REMINDER_CATEGORY);
        assertEquals("time_millis", DatabaseHelper.KEY_REMINDER_TIME);
        assertEquals("is_enabled", DatabaseHelper.KEY_REMINDER_ENABLED);
    }

    @Test
    public void testPeriodReminderTimeCalculation() {
        long lastPeriodMillis = 1000000000000L;
        long averageCycleDays = 28L;
        long daysBefore = 2L;

        long estimatedNextPeriodMillis = lastPeriodMillis + (averageCycleDays * 24L * 60 * 60 * 1000);
        long reminderTimeMillis = estimatedNextPeriodMillis - (daysBefore * 24L * 60 * 60 * 1000);

        assertEquals(lastPeriodMillis + (26L * 24 * 60 * 60 * 1000), reminderTimeMillis);
    }

    @Test
    public void testOvulationReminderOffsetCalculation() {
        long lastPeriodMillis = 1000000000000L;
        long averageCycleDays = 28L;

        long estimatedOvulationMillis = lastPeriodMillis + ((averageCycleDays - 14) * 24L * 60 * 60 * 1000);
        assertEquals(lastPeriodMillis + (14L * 24 * 60 * 60 * 1000), estimatedOvulationMillis);
    }
}
