package com.aicounsellor.backend.universities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortlistResponse {
    private String status; // shortlisted/locked
    private String message;
}
