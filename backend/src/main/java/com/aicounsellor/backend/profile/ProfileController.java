package com.aicounsellor.backend.profile;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.aicounsellor.backend.profile.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public UserProfile get(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.getOrCreate(userId);
    }

    @PutMapping
    public UserProfile update(Authentication auth, @RequestBody UpdateProfileRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.update(userId, req);
    }

    @PostMapping("/complete")
    public UserProfile complete(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.completeOnboarding(userId);
    }
}
