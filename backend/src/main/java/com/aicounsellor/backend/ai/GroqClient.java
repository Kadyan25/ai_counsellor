package com.aicounsellor.backend.ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GroqClient implements LLMClient {

    private final WebClient webClient;

    @Value("${app.groq.apiKey}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.1-8b-instant}")
    private String model;

    public GroqClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    @Override
    public String name() {
        return "groq";
    }

    @Override
    public Map<String, Object> generateRaw(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GROQ_API_KEY is missing");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.2
        );

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
