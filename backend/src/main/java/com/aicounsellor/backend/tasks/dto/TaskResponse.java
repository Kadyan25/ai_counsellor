package com.aicounsellor.backend.tasks.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {
    private UUID id;
    private String title;
    private String status;
    private String source;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}
