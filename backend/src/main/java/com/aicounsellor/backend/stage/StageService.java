package com.aicounsellor.backend.stage;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aicounsellor.backend.profile.UserProfile;
import com.aicounsellor.backend.profile.UserProfileRepository;
import com.aicounsellor.backend.universities.UserUniversityRepository;

@Service
public class StageService {

    private final UserStageRepository stageRepo;
    private final UserProfileRepository profileRepo;
    private final UserUniversityRepository userUniRepo;

    public StageService(
            UserStageRepository stageRepo,
            UserProfileRepository profileRepo,
            UserUniversityRepository userUniRepo
    ) {
        this.stageRepo = stageRepo;
        this.profileRepo = profileRepo;
        this.userUniRepo = userUniRepo;
    }

    public UserStage getOrCreate(UUID userId) {
        return stageRepo.findById(userId).orElseGet(() -> {
            UserStage s = new UserStage();
            s.setUserId(userId);
            s.setStage(1);
            s.setUpdatedAt(OffsetDateTime.now());
            return stageRepo.save(s);
        });
    }

    public int recalculateStage(UUID userId) {
        int stage = 1;

        UserProfile profile = profileRepo.findById(userId).orElse(null);

        if (profile != null && profile.isOnboardingCompleted()) {
            stage = 2; // discovery unlocked
        }

        boolean hasShortlist = userUniRepo.findByUserId(userId).size() > 0;
        if (stage >= 2 && hasShortlist) {
            stage = 3; // finalize unlocked
        }

        boolean hasLocked = userUniRepo.existsByUserIdAndStatus(userId, "locked");
        if (stage >= 3 && hasLocked) {
            stage = 4; // applications unlocked
        }

        UserStage s = getOrCreate(userId);
        s.setStage(stage);
        s.setUpdatedAt(OffsetDateTime.now());
        stageRepo.save(s);

        return stage;
    }
}
