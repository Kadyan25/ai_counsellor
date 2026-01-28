package com.aicounsellor.backend.auth.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponse {
    private UUID id;
    private String name;
    private String email;
    private OffsetDateTime createdAt;
}
