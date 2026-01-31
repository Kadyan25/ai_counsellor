package com.aicounsellor.backend.ai;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AiJsonParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Map<String, Object> extractJson(Map<String, Object> raw) {
        try {
            String text = null;

            // ✅ Gemini format
            if (raw.containsKey("candidates")) {
                var candidates = (List<Map<String, Object>>) raw.get("candidates");
                var c0 = candidates.get(0);
                var content = (Map<String, Object>) c0.get("content");
                var parts = (List<Map<String, Object>>) content.get("parts");
                text = String.valueOf(parts.get(0).get("text"));
            }

            // ✅ OpenRouter / Perplexity format (OpenAI compatible)
            if (text == null && raw.containsKey("choices")) {
                var choices = (List<Map<String, Object>>) raw.get("choices");
                var m0 = (Map<String, Object>) choices.get(0).get("message");
                text = String.valueOf(m0.get("content"));
            }

            if (text == null) {
                throw new RuntimeException("Unknown AI response format");
            }

            // Strip ```json fences
            text = text.replace("```json", "").replace("```", "").trim();

            return mapper.readValue(text, Map.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI JSON output: " + e.getMessage());
        }
    }
}
