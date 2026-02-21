package com.aicounsellor.backend.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiModelRouter {

    @Value("${app.ai.provider:auto}")
    private String provider;

    private final GeminiClient gemini;
    private final GroqClient groq;

    public AiModelRouter(GeminiClient gemini, GroqClient groq) {
        this.gemini = gemini;
        this.groq = groq;
    }

    public Map<String, Object> generateRaw(String systemPrompt, String userPrompt) {
        String p = provider == null ? "auto" : provider.toLowerCase();

        if (!"auto".equals(p)) {
            return pick(p).generateRaw(systemPrompt, userPrompt);
        }

        // auto fallback order
        List<LLMClient> order = List.of(gemini, groq);

        RuntimeException last = null;
        for (LLMClient client : order) {
            try {
            	 System.out.println("AI_PROVIDER_USING: " + client.name());
                return client.generateRaw(systemPrompt, userPrompt);
            } catch (RuntimeException e) {
                last = e;
                System.out.println("AI_PROVIDER_FAIL: " + client.name() + " -> " + e.getMessage());
            }
        }

        throw new RuntimeException("All AI providers failed: " + (last != null ? last.getMessage() : ""));
    }

    private LLMClient pick(String p) {
        return switch (p) {
            case "gemini" -> gemini;
            case "groq" -> groq;
            default -> throw new RuntimeException("Unknown AI_PROVIDER: " + p);
        };
    }
}
