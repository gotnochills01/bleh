package com.example.mahila;

import org.junit.Test;
import static org.junit.Assert.*;

public class Phase1VerificationTest {

    @Test
    public void verifyDatabaseConstantsAndUpgradePath() {
        assertEquals("users", DatabaseHelper.TABLE_USERS);
        assertEquals("periods", DatabaseHelper.TABLE_PERIODS);
        assertEquals("symptoms", DatabaseHelper.TABLE_SYMPTOMS);
        assertEquals("contacts", DatabaseHelper.TABLE_CONTACTS);
        assertEquals("moods", DatabaseHelper.TABLE_MOODS);

        assertEquals("user_email", DatabaseHelper.KEY_USER_EMAIL);
        assertEquals("is_synced", DatabaseHelper.KEY_IS_SYNCED);
        assertEquals("date_of_birth", DatabaseHelper.KEY_DATE_OF_BIRTH);
        assertEquals("blood_group", DatabaseHelper.KEY_BLOOD_GROUP);
        assertEquals("city", DatabaseHelper.KEY_CITY);
        assertEquals("emergency_notes", DatabaseHelper.KEY_EMERGENCY_NOTES);
        assertEquals("weight", DatabaseHelper.KEY_WEIGHT);
        assertEquals("height", DatabaseHelper.KEY_HEIGHT);
    }
}
