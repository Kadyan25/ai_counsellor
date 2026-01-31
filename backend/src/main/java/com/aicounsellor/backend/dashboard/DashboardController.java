package com.aicounsellor.backend.dashboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aicounsellor.backend.profile.ProfileService;
import com.aicounsellor.backend.stage.StageService;
import com.aicounsellor.backend.tasks.TaskService;
import com.aicounsellor.backend.universities.UniversityService;
import com.aicounsellor.backend.universities.UserUniversity;

@RestController
public class DashboardController {

    private final ProfileService profileService;
    private final StageService stageService;
    private final TaskService taskService;
    private final UniversityService universityService;

    public DashboardController(
            ProfileService profileService,
            StageService stageService,
            TaskService taskService,
            UniversityService universityService
    ) {
        this.profileService = profileService;
        this.stageService = stageService;
        this.taskService = taskService;
        this.universityService = universityService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();

        int stage = stageService.recalculateStage(userId);

        List<UserUniversity> my = universityService.myShortlist(userId);
        boolean hasLocked = my.stream().anyMatch(x -> "locked".equalsIgnoreCase(x.getStatus()));

        return Map.of(
                "stage", stage,
                "profile", profileService.getOrCreate(userId),
                "shortlist", my,
                "hasLocked", hasLocked,
                "tasks", taskService.myTasks(userId)
        );
    }
}
