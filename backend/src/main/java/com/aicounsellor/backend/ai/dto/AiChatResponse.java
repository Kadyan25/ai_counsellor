package com.aicounsellor.backend.ai.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private List<Map<String, Object>> actions;
    private Map<String, Object> snapshot;
}
