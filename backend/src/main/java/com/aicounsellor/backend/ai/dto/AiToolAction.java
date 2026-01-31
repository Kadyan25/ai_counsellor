package com.aicounsellor.backend.ai.dto;

import java.util.Map;

import lombok.Data;

@Data
public class AiToolAction {
    private String type;              // shortlist | lock | unlock | create_task
    private Map<String, Object> args; // dynamic args
}
