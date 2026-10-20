package com.mahila.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiAiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${mahila.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${mahila.ai.gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiAiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Optional<String> generateAnswer(String prompt, String language, String category) {
        if (prompt == null || prompt.isBlank()) {
            return Optional.empty();
        }
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            StringBuilder systemInstruction = new StringBuilder(
                    "You are Mahila AI, a women’s health assistant. Provide concise, respectful, general wellness information only. " +
                            "Do not diagnose medical conditions, recommend emergency treatment, or claim personal medical certainty. " +
                            "Use user language code " + (language == null || language.isBlank() ? "en" : language) + "."
            );

            if (category != null && !category.isBlank()) {
                systemInstruction.append(" Focus on the category: ").append(category);
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            Map<String, Object> userMessage = new LinkedHashMap<>();
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("text", prompt);
            userMessage.put("role", "user");
            userMessage.put("parts", new Object[]{textPart});

            Map<String, Object> systemPart = new LinkedHashMap<>();
            systemPart.put("text", systemInstruction.toString());

            payload.put("contents", new Object[]{userMessage});
            payload.put("systemInstruction", Map.of("parts", new Object[]{systemPart}));
            payload.put("generationConfig", Map.of(
                    "temperature", 0.3,
                    "topK", 40,
                    "topP", 0.9,
                    "maxOutputTokens", 300
            ));

            String requestBody = objectMapper.writeValueAsString(payload);
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            String text = textNode.asText(null);
            return (text == null || text.isBlank()) ? Optional.empty() : Optional.of(text.trim());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
