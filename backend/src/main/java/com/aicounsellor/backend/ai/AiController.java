package com.aicounsellor.backend.ai;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.aicounsellor.backend.ai.dto.AiChatRequest;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService service;
    

    public AiController(AiService service) {
        this.service = service;
       
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(Authentication auth, @RequestBody AiChatRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.chat(userId, req.getMessage());
    }
    @GetMapping("/history")
    public List<AiMessage> history(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.getHistory(userId);
    }

}
