package com.aicounsellor.backend.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.aicounsellor.backend.tasks.dto.GenerateTasksResponse;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserTask> myTasks(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.myTasks(userId);
    }

    @PostMapping("/generate")
    public GenerateTasksResponse generate(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.generate(userId);
    }

    @PatchMapping("/{id}/done")
    public UserTask done(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.markDone(userId, id);
    }
}
