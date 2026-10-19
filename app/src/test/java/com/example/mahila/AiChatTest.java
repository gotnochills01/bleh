package com.example.mahila;

import org.junit.Test;
import static org.junit.Assert.*;

public class AiChatTest {

    @Test
    public void testEducationalFallbackPcosPrompt() {
        String response = AiChatActivity.getEducationalFallbackForPrompt("What is PCOS and how to manage it?");
        assertTrue(response.contains("Polycystic Ovary Syndrome"));
        assertTrue(response.contains("low-GI"));
    }

    @Test
    public void testEducationalFallbackCrampsPrompt() {
        String response = AiChatActivity.getEducationalFallbackForPrompt("How can I relieve severe cramps?");
        assertTrue(response.contains("heating pad"));
        assertTrue(response.contains("chamomile"));
    }

    @Test
    public void testEducationalFallbackNutritionPrompt() {
        String response = AiChatActivity.getEducationalFallbackForPrompt("What food should I eat on my period?");
        assertTrue(response.contains("iron-rich"));
        assertTrue(response.contains("spinach"));
    }

    @Test
    public void testEducationalFallbackNullPrompt() {
        String response = AiChatActivity.getEducationalFallbackForPrompt(null);
        assertNotNull(response);
        assertTrue(response.contains("reproductive health"));
    }
}
