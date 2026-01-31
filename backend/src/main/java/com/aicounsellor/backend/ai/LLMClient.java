package com.aicounsellor.backend.ai;

import java.util.Map;

public interface LLMClient {
    Map<String, Object> generateRaw(String systemPrompt, String userPrompt);
    String name();
}
