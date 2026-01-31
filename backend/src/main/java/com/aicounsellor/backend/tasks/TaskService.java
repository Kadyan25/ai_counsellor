package com.aicounsellor.backend.tasks;

import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import com.aicounsellor.backend.profile.UserProfile;
import com.aicounsellor.backend.profile.UserProfileRepository;
import com.aicounsellor.backend.tasks.dto.GenerateTasksResponse;

@Service
public class TaskService {

    private final UserTaskRepository repo;
    private final UserProfileRepository profileRepo;

    public TaskService(UserTaskRepository repo, UserProfileRepository profileRepo) {
        this.repo = repo;
        this.profileRepo = profileRepo;
    }

    public List<UserTask> myTasks(UUID userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public GenerateTasksResponse generate(UUID userId) {
        UserProfile profile = profileRepo.findById(userId).orElse(null);
        if (profile == null || !profile.isOnboardingCompleted()) {
            throw new RuntimeException("Complete onboarding first to generate tasks.");
        }

        List<String> titles = new ArrayList<>();

        // stage-2 tasks (discovery)
        titles.add("Finalize shortlist: pick at least 6 universities (2 dream, 2 target, 2 safe)");
        titles.add("Create tuition + living cost plan for selected countries");

        // readiness tasks
        if (profile.getIeltsStatus() == null || profile.getIeltsStatus().toLowerCase().contains("not")) {
            titles.add("Book IELTS/TOEFL exam date and create prep schedule");
        }
        if (profile.getGreStatus() == null || profile.getGreStatus().toLowerCase().contains("not")) {
            titles.add("Decide if GRE/GMAT is required for target universities");
        }
        if (profile.getSopStatus() == null || profile.getSopStatus().toLowerCase().contains("not")) {
            titles.add("Start SOP draft (collect projects, internships, achievements)");
        }

        int created = 0;
        for (String t : titles) {
            // avoid duplicates: basic check
            boolean already = repo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .anyMatch(x -> x.getTitle().equalsIgnoreCase(t));
            if (already) continue;

            UserTask task = new UserTask();
            task.setId(UUID.randomUUID());
            task.setUserId(userId);
            task.setTitle(t);
            task.setStatus("pending");
            task.setSource("ai");
            task.setCreatedAt(OffsetDateTime.now());

            repo.save(task);
            created++;
        }

        return new GenerateTasksResponse(created, "Tasks generated/updated.");
    }

    public UserTask markDone(UUID userId, UUID taskId) {
        UserTask task = repo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Not allowed.");
        }

        task.setStatus("done");
        task.setCompletedAt(OffsetDateTime.now());
        return repo.save(task);
    }

	public void recalculateForUser(UUID userId) {
		// TODO Auto-generated method stub
		
	}
}
