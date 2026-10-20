package com.example.mahila;

import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;

import static org.junit.Assert.*;

public class MedicalLocationHelperTest {

    @Test
    public void testNullLocationIsRejected() {
        assertFalse(MedicalLocationHelper.isValidLocation(null));
    }

    @Test
    public void testValidLocationFormatsCoordinates() {
        String formatted = MedicalLocationHelper.formatLocation(new LatLng(28.6139, 77.2090));
        assertEquals("28.6139, 77.2090", formatted);
    }

    @Test
    public void testErrorMessageDoesNotMentionImplicitDelhiFallback() {
        String message = MedicalLocationHelper.buildUnavailableMessage("Gynecologists");
        assertFalse(message.contains("New Delhi"));
        assertTrue(message.contains("Location unavailable"));
    }
}
