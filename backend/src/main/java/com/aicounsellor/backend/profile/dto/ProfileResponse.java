package com.aicounsellor.backend.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private boolean onboardingCompleted;
    private Object profile; // simple for MVP
}
