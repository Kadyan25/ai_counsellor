package com.aicounsellor.backend.ai;

import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import com.aicounsellor.backend.profile.ProfileService;
import com.aicounsellor.backend.profile.UserProfile;
import com.aicounsellor.backend.stage.StageService;
import com.aicounsellor.backend.tasks.UserTask;
import com.aicounsellor.backend.tasks.UserTaskRepository;
import com.aicounsellor.backend.tasks.TaskService;
import com.aicounsellor.backend.universities.UniversityService;

@Service
public class AiService {

    private final AiModelRouter router;
    private final AiJsonParser parser;

    private final ProfileService profileService;
    private final StageService stageService;
    private final UniversityService universityService;
    private final TaskService taskService;
    private final UserTaskRepository taskRepo;
    private final AiMessageRepository aiMessageRepo;


    public AiService(
            AiModelRouter router,
            AiJsonParser parser,
            ProfileService profileService,
            StageService stageService,
            UniversityService universityService,
            TaskService taskService,
            UserTaskRepository taskRepo,
            AiMessageRepository aiMessageRepo
    ) {
        this.router = router;
        this.parser = parser;
        this.profileService = profileService;
        this.stageService = stageService;
        this.universityService = universityService;
        this.taskService = taskService;
        this.taskRepo = taskRepo;
        this.aiMessageRepo = aiMessageRepo;
    }

    
    @SuppressWarnings("unchecked")
    public Map<String, Object> chat(UUID userId, String message) {
    	
        AiMessage userMsg = new AiMessage();
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreatedAt(OffsetDateTime.now());
        aiMessageRepo.save(userMsg);

        int stage = stageService.recalculateStage(userId);
        UserProfile profile = profileService.getOrCreate(userId);

        // strict gating
        boolean onboardingComplete = profile.isOnboardingCompleted();

        String gating = onboardingComplete
                ? "Onboarding complete. Recommend universities and take actions if helpful."
                : "Onboarding incomplete. DO NOT recommend universities. Ask onboarding questions only.";

        // IMPORTANT: limit available universities sent to model (avoid huge prompt)
        List<Map<String, Object>> availableUnis = new ArrayList<>();
        if (onboardingComplete) {
            var discovered = universityService.discover(userId);
            for (int i = 0; i < Math.min(discovered.size(), 25); i++) {
                var u = discovered.get(i);
                availableUnis.add(Map.of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "country", u.getCountry(),
                        "yearlyCostUsd", u.getYearlyCostUsd(),
                        "bucket", u.getBucket(),
                        "acceptanceChance", u.getAcceptanceChance(),
                        "risk", u.getRisk()
                ));
            }
        }

        String systemPrompt = """
        		You are AI Counsellor for a stage-based study abroad platform.

        		You MUST respond in strict JSON only (no markdown, no text outside JSON).

        		Output JSON schema:
        		{
        		  "reply": "string",
        		  "actions": [
        		    {"type": "shortlist", "args": {"universityId": "<uuid>"}},
        		    {"type": "lock", "args": {"universityId": "<uuid>"}},
        		    {"type": "unlock", "args": {"universityId": "<uuid>"}},
        		    {"type": "lock_recent_shortlisted", "args": {}},
        		    {"type": "create_task", "args": {"title": "string"}}
        		  ]
        		}

        		CRITICAL RULES:
        		- NEVER invent (hallucinate) a university name or ID.
        		- You are ONLY allowed to mention universities that exist in:
        		  (1) shortlist OR (2) availableUniversitiesTop context.
        		- If you cannot find an ID, do NOT guess. Ask user to shortlist first.
        		- For locking, prefer "lock_recent_shortlisted" unless the user explicitly provides an ID.
        		- If onboarding incomplete: actions MUST be [] and guide onboarding only.
        		- If you include an action, your reply MUST confirm what was done.
        		- Do not ask the user to choose if you already executed lock_recent_shortlisted.


        		BEHAVIOR BY STAGE:
        		- stage=1: onboarding questions only
        		- stage=2: recommend universities + shortlist
        		- stage=3: push locking at least one university
        		- stage=4: focus on application readiness tasks

        		ACTION LIMIT:
        		- Max 3 actions.
        		""";


        Map<String, Object> profileMap = new LinkedHashMap<>();
        profileMap.put("educationLevel", profile.getEducationLevel());
        profileMap.put("major", profile.getMajor());
        profileMap.put("gradYear", profile.getGradYear());
        profileMap.put("gpa", profile.getGpa());
        profileMap.put("intendedDegree", profile.getIntendedDegree());
        profileMap.put("fieldOfStudy", profile.getFieldOfStudy());
        profileMap.put("intakeYear", profile.getIntakeYear());
        profileMap.put("preferredCountries", profile.getPreferredCountries());
        profileMap.put("budgetPerYear", profile.getBudgetPerYear());
        profileMap.put("fundingPlan", profile.getFundingPlan());
        profileMap.put("ieltsStatus", profile.getIeltsStatus());
        profileMap.put("greStatus", profile.getGreStatus());
        profileMap.put("sopStatus", profile.getSopStatus());
        profileMap.put("onboardingCompleted", profile.isOnboardingCompleted());

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("stage", stage);
        context.put("gating", gating);
        context.put("profile", profileMap);


        if (onboardingComplete) {
            context.put("shortlist", universityService.myShortlist(userId));
            context.put("availableUniversitiesTop", availableUnis);
        }

        String userPrompt = """
                CONTEXT:
                %s

                USER_MESSAGE:
                %s
                """.formatted(context.toString(), message);

        Map<String, Object> json;

        try {
            // Call AI provider (auto fallback)
            Map<String, Object> raw = router.generateRaw(systemPrompt, userPrompt);

            if (raw == null || raw.isEmpty()) {
                throw new RuntimeException("AI returned empty response");
            }

            json = parser.extractJson(raw);

        } catch (Exception e) {

            // 🔒 HARD SAFETY FALLBACK — NEVER BREAK JSON CONTRACT
            json = Map.of(
                "reply", "I’m warming up. Please click retry — your data is safe.",
                "actions", List.of()
            );
        }


        String reply = json.get("reply") instanceof String
                ? (String) json.get("reply")
                : "I’m warming up. Please retry.";

        AiMessage aiMsg = new AiMessage();
        aiMsg.setUserId(userId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        aiMsg.setCreatedAt(OffsetDateTime.now());
        aiMessageRepo.save(aiMsg);
        List<Map<String, Object>> actions = (List<Map<String, Object>>) json.getOrDefault("actions", List.of());

        // enforce gating
        if (!onboardingComplete && actions != null && !actions.isEmpty()) {
            actions = List.of(); // ignore actions
            reply = reply + "\n\n(Please complete onboarding first.)";
        }

        // execute actions
        List<Map<String, Object>> executed = new ArrayList<>();
        int actionCount = 0;

        for (Map<String, Object> a : actions) {
            if (actionCount >= 3) break; // hard limit
            actionCount++;

            try {
                String type = String.valueOf(a.get("type"));
                Map<String, Object> args = (Map<String, Object>) a.getOrDefault("args", Map.of());

                Map<String, Object> result = executeAction(userId, type, args);
                executed.add(Map.of("type", type, "args", args, "result", result));
            } catch (Exception e) {
                executed.add(Map.of("error", e.getMessage(), "action", a));
            }
        }

        // refresh snapshot
        int newStage = stageService.recalculateStage(userId);

        Map<String, Object> snapshot = Map.of(
                "stage", newStage,
                "profile", profileService.getOrCreate(userId),
                "shortlist", universityService.myShortlist(userId),
                "tasks", taskService.myTasks(userId)
        );

        return Map.of(
                "reply", reply,
                "actions", executed,
                "snapshot", snapshot
        );
    }

    private Map<String, Object> executeAction(UUID userId, String type, Map<String, Object> args) {

    	if ("lock_recent_shortlisted".equalsIgnoreCase(type)) {
    	    var r = universityService.lockRecentShortlisted(userId);
    	    return Map.of("status", r.getStatus(), "message", r.getMessage());
    	}

        if ("shortlist".equalsIgnoreCase(type)) {
            UUID id = UUID.fromString(String.valueOf(args.get("universityId")));
            var r = universityService.shortlist(userId, id);
            return Map.of("status", r.getStatus(), "message", r.getMessage());
        }

        if ("lock".equalsIgnoreCase(type)) {
            UUID id = UUID.fromString(String.valueOf(args.get("universityId")));
            var r = universityService.lock(userId, id);
            return Map.of("status", r.getStatus(), "message", r.getMessage());
        }

        if ("unlock".equalsIgnoreCase(type)) {
            UUID id = UUID.fromString(String.valueOf(args.get("universityId")));
            var r = universityService.unlock(userId, id);
            return Map.of("status", r.getStatus(), "message", r.getMessage());
        }

        if ("create_task".equalsIgnoreCase(type)) {
            String title = String.valueOf(args.get("title"));

            if (title == null || title.isBlank()) {
                throw new RuntimeException("Task title missing");
            }

            // prevent duplicates
            boolean exists = taskRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .anyMatch(t -> t.getTitle().equalsIgnoreCase(title));

            if (exists) {
                return Map.of("created", false, "message", "Task already exists");
            }

            UserTask t = new UserTask();
            t.setId(UUID.randomUUID());
            t.setUserId(userId);
            t.setTitle(title);
            t.setStatus("pending");
            t.setSource("ai");
            t.setCreatedAt(OffsetDateTime.now());

            taskRepo.save(t);
            return Map.of("created", true, "taskTitle", title);
        }

        return Map.of("ignored", true, "reason", "Unknown action type: " + type);
    }



public List<AiMessage> getHistory(UUID userId) {
    return aiMessageRepo.findByUserIdOrderByCreatedAtAsc(userId);
}


}
