package com.example.mahila;

import org.junit.Test;
import static org.junit.Assert.*;

public class EmergencyContactsTest {

    @Test
    public void testDialUriConstruction() {
        String uri1 = EmergencyContactsActivity.buildDialUriString("1091");
        assertEquals("tel:1091", uri1);

        String uri2 = EmergencyContactsActivity.buildDialUriString("+91 98765-43210");
        assertEquals("tel:919876543210", uri2);

        String uri3 = EmergencyContactsActivity.buildDialUriString(null);
        assertEquals("tel:", uri3);
    }

    @Test
    public void testUserEmailNormalizationAndValidation() {
        assertEquals("user@example.com", EmergencyContactsActivity.normalizeUserEmail(" User@Example.com "));
        assertEquals("Asha Sharma", EmergencyContactsActivity.sanitizeContactName("  asha   sharma  "));
        assertTrue(EmergencyContactsActivity.isValidPhoneNumber("+91 98765 43210"));
        assertFalse(EmergencyContactsActivity.isValidPhoneNumber("123"));
    }
}
