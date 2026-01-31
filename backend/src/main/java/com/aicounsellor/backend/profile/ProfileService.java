package com.aicounsellor.backend.profile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aicounsellor.backend.profile.dto.UpdateProfileRequest;
import com.aicounsellor.backend.stage.StageService;
import com.aicounsellor.backend.tasks.TaskService;
import com.aicounsellor.backend.universities.UniversityService;

@Service
public class ProfileService {

    private final UserProfileRepository repo;
    private final StageService stageService;
    private final UniversityService universityService;
    private final TaskService taskService;

    public ProfileService(
            UserProfileRepository repo,
            StageService stageService,
            UniversityService universityService,
            TaskService taskService
    ) {
        this.repo = repo;
        this.stageService = stageService;
        this.universityService = universityService;
        this.taskService = taskService;
    }

    public UserProfile getOrCreate(UUID userId) {
        return repo.findById(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUserId(userId);
            p.setUpdatedAt(OffsetDateTime.now());

            UserProfile saved = repo.save(p);

            stageService.recalculateStage(userId);
            universityService.recalculateForUser(userId);
            taskService.recalculateForUser(userId);

            return saved;
        });
    }

    // Existing form-based update (unchanged)
    public UserProfile update(UUID userId, UpdateProfileRequest req) {
        UserProfile p = getOrCreate(userId);

        p.setEducationLevel(req.educationLevel);
        p.setMajor(req.major);
        p.setGradYear(req.gradYear);
        p.setGpa(req.gpa);

        p.setIntendedDegree(req.intendedDegree);
        p.setFieldOfStudy(req.fieldOfStudy);
        p.setIntakeYear(req.intakeYear);
        p.setPreferredCountries(req.preferredCountries);

        p.setBudgetPerYear(req.budgetPerYear);
        p.setFundingPlan(req.fundingPlan);

        p.setIeltsStatus(req.ieltsStatus);
        p.setGreStatus(req.greStatus);
        p.setSopStatus(req.sopStatus);

        p.setUpdatedAt(OffsetDateTime.now());

        UserProfile saved = repo.save(p);
        stageService.recalculateStage(userId);

        return saved;
    }

//    // 🔹 AI FIELD-BY-FIELD UPDATE
//    public void updateField(UUID userId, String field, Object value) {
//        UserProfile p = getOrCreate(userId);
//
//        switch (field) {
//            case "educationLevel" -> p.setEducationLevel(value.toString());
//            case "major" -> p.setMajor(value.toString());
//            case "gradYear" -> p.setGradYear(Integer.parseInt(value.toString()));
//            case "gpa" -> p.setGpa(Double.parseDouble(value.toString()));
//            case "intendedDegree" -> p.setIntendedDegree(value.toString());
//            case "fieldOfStudy" -> p.setFieldOfStudy(value.toString());
//            case "intakeYear" -> p.setIntakeYear(Integer.parseInt(value.toString()));
//            case "budgetPerYear" -> p.setBudgetPerYear(Integer.parseInt(value.toString()));
//
//            case "preferredCountries" -> {
//                List<?> list = (List<?>) value;
//                String[] arr = list.stream()
//                        .map(Object::toString)
//                        .toArray(String[]::new);
//                p.setPreferredCountries(arr);
//            }
//
//            default -> throw new RuntimeException("Invalid profile field: " + field);
//        }
//
//        p.setUpdatedAt(OffsetDateTime.now());
//        repo.save(p);
//    }

    public UserProfile completeOnboarding(UUID userId) {
        UserProfile p = getOrCreate(userId);
        p.setOnboardingCompleted(true);
        p.setUpdatedAt(OffsetDateTime.now());
        UserProfile saved = repo.save(p);

        stageService.recalculateStage(userId);

        return saved;
    }

//    
//    private boolean hasAllRequiredFields(UserProfile p) {
//        return p.getEducationLevel() != null
//            && p.getMajor() != null
//            && p.getGradYear() != null
//            && p.getGpa() != null
//            && p.getIntendedDegree() != null
//            && p.getFieldOfStudy() != null
//            && p.getIntakeYear() != null
//            && p.getBudgetPerYear() != null
//            && p.getPreferredCountries() != null
//            && p.getPreferredCountries().length > 0;
//    }
}
