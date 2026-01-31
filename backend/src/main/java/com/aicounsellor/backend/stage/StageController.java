package com.aicounsellor.backend.stage;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StageController {

    private final StageService stageService;

    public StageController(StageService stageService) {
        this.stageService = stageService;
    }

    @GetMapping("/stage")
    public Map<String, Object> stage(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        int stage = stageService.recalculateStage(userId);
        return Map.of("stage", stage);
    }
}
