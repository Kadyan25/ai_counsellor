package com.aicounsellor.backend.universities.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UniversityDiscoverResponse {
    private UUID id;
    private String name;
    private String country;

    private int yearlyCostUsd;
    private String difficulty;

    private String bucket;           // DREAM / TARGET / SAFE
    private String acceptanceChance; // LOW / MEDIUM / HIGH
    private String risk;             // LOW / MEDIUM / HIGH

    private String reason;           // 1-line AI style explanation (rule based for now)
}
