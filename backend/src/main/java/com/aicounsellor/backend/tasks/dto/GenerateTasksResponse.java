package com.aicounsellor.backend.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateTasksResponse {
    private int createdCount;
    private String message;
}
