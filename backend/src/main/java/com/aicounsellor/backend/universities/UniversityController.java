package com.aicounsellor.backend.universities;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.aicounsellor.backend.universities.dto.ShortlistResponse;
import com.aicounsellor.backend.universities.dto.UniversityDiscoverResponse;

@RestController
@RequestMapping("/universities")
public class UniversityController {

    private final UniversityService service;

    public UniversityController(UniversityService service) {
        this.service = service;
    }

    @GetMapping("/discover")
    public List<UniversityDiscoverResponse> discover(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.discover(userId);
    }

    @GetMapping("/my")
    public List<UserUniversity> my(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.myShortlist(userId);
    }

    @PostMapping("/{id}/shortlist")
    public ShortlistResponse shortlist(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.shortlist(userId, id);
    }

    @PostMapping("/{id}/lock")
    public ShortlistResponse lock(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.lock(userId, id);
    }

    @PostMapping("/{id}/unlock")
    public ShortlistResponse unlock(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.unlock(userId, id);
    }
}
