package com.aicounsellor.backend.ai;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class AiWarmupController {

    private final AiModelRouter router;

    public AiWarmupController(AiModelRouter router) {
        this.router = router;
    }

    @GetMapping("/ai-warmup")
    public Map<String, Object> warmup() {
        try {
            // minimal no-context call
            router.generateRaw(
                "You are a health check. Reply with OK in JSON.",
                "ping"
            );
        } catch (Exception e) {
            // swallow errors – warmup must never fail
        }

        return Map.of("status", "OK");
    }
}
