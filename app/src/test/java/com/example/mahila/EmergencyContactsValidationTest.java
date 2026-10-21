package com.example.mahila;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmergencyContactsValidationTest {

    @Test
    public void sanitizeContactName_formatsNamesConsistently() {
        assertEquals("Asha Rani", EmergencyContactsActivity.sanitizeContactName(" asha   rani "));
        assertEquals("Nisha", EmergencyContactsActivity.sanitizeContactName("nisha"));
        assertEquals("", EmergencyContactsActivity.sanitizeContactName("   "));
    }

    @Test
    public void validPhoneNumber_acceptsIndianAndInternationalFormats() {
        assertTrue(EmergencyContactsActivity.isValidPhoneNumber("9876543210"));
        assertTrue(EmergencyContactsActivity.isValidPhoneNumber("+91 98765 43210"));
        assertTrue(EmergencyContactsActivity.isValidPhoneNumber("(555) 123-4567"));
        assertFalse(EmergencyContactsActivity.isValidPhoneNumber("12345"));
        assertFalse(EmergencyContactsActivity.isValidPhoneNumber(""));
    }

    @Test
    public void dialUriStripsFormattingBeforeDialing() {
        assertEquals("tel:919876543210", EmergencyContactsActivity.buildDialUriString("+91 98765-43210"));
        assertEquals("tel:", EmergencyContactsActivity.buildDialUriString(null));
    }
}
