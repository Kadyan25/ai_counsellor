package com.aicounsellor.backend.ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GeminiClient implements LLMClient {

    private final WebClient webClient;

    @Value("${app.gemini.apiKey}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-1.5-flash}")
    private String model;

    public GeminiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public Map<String, Object> generateRaw(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY is missing");
        }

        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("role", "user", "parts", new Object[]{
                                Map.of("text", systemPrompt + "\n\nUSER:\n" + userPrompt)
                        })
                }
        );

        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
