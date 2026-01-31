package com.aicounsellor.backend.ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenRouterClient implements LLMClient {

    private final WebClient webClient;

    @Value("${app.openrouter.apiKey}")
    private String apiKey;

    @Value("${app.openrouter.model}")
    private String model;

    public OpenRouterClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai")
                .build();
    }

    @Override
    public String name() {
        return "openrouter";
    }

    @Override
    public Map<String, Object> generateRaw(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENROUTER_API_KEY is missing");
        }

        // OpenRouter: POST /api/v1/chat/completions
        // https://openrouter.ai/docs#requests
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.3
        );

        return webClient.post()
                .uri("/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost") // recommended by OpenRouter
                .header("X-Title", "AI Counsellor Hackathon")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
