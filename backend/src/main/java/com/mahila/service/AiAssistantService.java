package com.mahila.service;

import com.mahila.dto.AiChatRequest;
import com.mahila.dto.AiChatResponse;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AiAssistantService {

    private static final String DISCLAIMER = "Disclaimer: The AI Assistant provides general educational information only. It does not provide medical diagnoses or replace professional healthcare consultations. Please speak to a doctor for personal medical advice.";

    private final GeminiAiService geminiAiService;

    public AiAssistantService(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    public AiChatResponse generateResponse(AiChatRequest request) {
        String prompt = request != null && request.getPrompt() != null ? request.getPrompt().trim() : "";
        String category = request != null && request.getCategory() != null && !request.getCategory().isBlank()
                ? request.getCategory() : "GENERAL";
        String language = request != null && request.getLanguage() != null && !request.getLanguage().isBlank()
                ? request.getLanguage() : "en";

        if (prompt.isEmpty()) {
            return buildResponse("Please enter a question before using the Mahila AI assistant.", "General Wellness");
        }

        Optional<String> geminiResponse = geminiAiService.generateAnswer(prompt, language, category);
        if (geminiResponse.isPresent()) {
            return buildResponse(geminiResponse.get(), category);
        }

        return buildResponse(
                "Gemini AI is currently unavailable, so this is a local general wellness fallback and not a Gemini-generated answer. " +
                        getKeywordFallback(prompt),
                "General Wellness"
        );
    }

    private AiChatResponse buildResponse(String response, String category) {
        return AiChatResponse.builder()
                .response(response)
                .disclaimer(DISCLAIMER)
                .recommendedCategory(category == null || category.isBlank() ? "General Wellness" : category)
                .build();
    }

    private String getKeywordFallback(String prompt) {
        String lower = prompt.toLowerCase(Locale.US);
        if (lower.contains("pcos") || lower.contains("ovary")) {
            return "Polycystic Ovary Syndrome (PCOS) is a common hormonal condition. Moderate exercise, a balanced low-GI diet, stress management, and regular cycle tracking can support symptom management. Please consult a doctor for personal medical advice.";
        } else if (lower.contains("cramp") || lower.contains("pain")) {
            return "Menstrual cramps may be eased with a warm compress, hydration, and gentle movement. If pain is severe or persistent, talk with a healthcare professional.";
        } else if (lower.contains("food") || lower.contains("diet") || lower.contains("nutrition")) {
            return "Iron-rich foods such as lentils and spinachpaired with vitamin C sources can support well-being during your cycle. A balanced diet and hydration matter most, and individual needs vary.";
        } else if (lower.contains("ovulation") || lower.contains("fertile")) {
            return "Ovulation usually happens about 14 days before the next expected period. Signs can include cervical mucus changes and a slight temperature rise, but your cycle varies.";
        }
        return "Maintaining balanced nutrition, hydration, sleep, and regular cycle tracking can support overall reproductive wellness.";
    }
}
