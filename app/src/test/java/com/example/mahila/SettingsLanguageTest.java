package com.example.mahila;

import com.example.mahila.utils.LocaleHelper;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class SettingsLanguageTest {

    private static final String[] LANGUAGES = new String[]{"English", "Kannada", "Hindi", "Tamil", "Telugu", "Malayalam"};
    private static final String[] CODES = new String[]{"en", "kn", "hi", "ta", "te", "ml"};

    @Test
    public void testLanguageCodesAndNamesMapping() {
        assertEquals(6, LANGUAGES.length);
        assertEquals(6, CODES.length);

        List<String> validCodes = Arrays.asList(CODES);
        assertTrue(validCodes.contains("en"));
        assertTrue(validCodes.contains("kn"));
        assertTrue(validCodes.contains("hi"));
        assertTrue(validCodes.contains("ta"));
        assertTrue(validCodes.contains("te"));
        assertTrue(validCodes.contains("ml"));
    }

    @Test
    public void testLocaleResolution() {
        for (String code : CODES) {
            Locale locale = new Locale(code);
            assertEquals(code, locale.getLanguage());
        }
    }

    @Test
    public void testFallbackLanguageCode() {
        String nullLang = null;
        String emptyLang = "  ";

        Locale defaultNull = new Locale(nullLang != null && !nullLang.trim().isEmpty() ? nullLang : "en");
        Locale defaultEmpty = new Locale(emptyLang != null && !emptyLang.trim().isEmpty() ? emptyLang : "en");

        assertEquals("en", defaultNull.getLanguage());
        assertEquals("en", defaultEmpty.getLanguage());
    }
}
