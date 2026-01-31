package com.aicounsellor.backend.universities;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aicounsellor.backend.profile.UserProfile;
import com.aicounsellor.backend.profile.UserProfileRepository;
import com.aicounsellor.backend.stage.StageService;
import com.aicounsellor.backend.universities.dto.ShortlistResponse;
import com.aicounsellor.backend.universities.dto.UniversityDiscoverResponse;

@Service
public class UniversityService {

    private final UniversityRepository universityRepo;
    private final UserUniversityRepository userUniRepo;
    private final UserProfileRepository profileRepo;
    private final StageService stageService;

    public UniversityService(
            UniversityRepository universityRepo,
            UserUniversityRepository userUniRepo,
            UserProfileRepository profileRepo,
            StageService stageService
    ) {
        this.universityRepo = universityRepo;
        this.userUniRepo = userUniRepo;
        this.profileRepo = profileRepo;
        this.stageService = stageService;
    }

    public List<UniversityDiscoverResponse> discover(UUID userId) {
        UserProfile profile = profileRepo.findById(userId).orElse(null);
        if (profile == null || !profile.isOnboardingCompleted()) {
            throw new RuntimeException("Complete onboarding first.");
        }

        List<String> preferredCountries = profile.getPreferredCountries() != null
                ? Arrays.asList(profile.getPreferredCountries())
                : List.of();

        Integer budget = profile.getBudgetPerYear();

        List<University> unis = preferredCountries.isEmpty()
                ? universityRepo.findAll()
                : universityRepo.findByCountryIn(preferredCountries);

        // basic filters
        if (budget != null) {
            unis = unis.stream()
                    .filter(u -> u.getYearlyCostUsd() <= budget + 15000) // allow some stretch
                    .collect(Collectors.toList());
        }

        // scoring (simple)
        double gpa = profile.getGpa() != null ? profile.getGpa() : 0.0;

        List<UniversityDiscoverResponse> out = new ArrayList<>();
        for (University u : unis) {
            String bucket = bucket(profile, u, gpa);
            String acceptance = acceptanceChance(profile, u, gpa);
            String risk = riskLevel(profile, u, gpa);
            String reason = reason(profile, u, bucket, acceptance);

            out.add(new UniversityDiscoverResponse(
                    u.getId(),
                    u.getName(),
                    u.getCountry(),
                    u.getYearlyCostUsd(),
                    u.getDifficulty(),
                    bucket,
                    acceptance,
                    risk,
                    reason
            ));
        }

        // sort: DREAM first then TARGET then SAFE
        Map<String, Integer> rank = Map.of("DREAM", 1, "TARGET", 2, "SAFE", 3);
        out.sort(Comparator.comparingInt(x -> rank.getOrDefault(x.getBucket(), 99)));

        return out;
    }

    private String bucket(UserProfile p, University u, double gpa) {
        String diff = safe(u.getDifficulty()).toLowerCase();

        boolean expensive = p.getBudgetPerYear() != null && u.getYearlyCostUsd() > p.getBudgetPerYear();

        if ("high".equals(diff)) {
            if (gpa >= 3.4) return "DREAM";
            return "TARGET";
        }

        if ("medium".equals(diff)) {
            if (expensive) return "TARGET";
            return "TARGET";
        }

        // low difficulty
        return "SAFE";
    }

    private String acceptanceChance(UserProfile p, University u, double gpa) {
        Double min = u.getMinGpa();
        if (min == null) return "MEDIUM";
        if (gpa >= min + 0.3) return "HIGH";
        if (gpa >= min) return "MEDIUM";
        return "LOW";
    }

    private String riskLevel(UserProfile p, University u, double gpa) {
        int risk = 0;

        // budget risk
        if (p.getBudgetPerYear() != null && u.getYearlyCostUsd() > p.getBudgetPerYear()) risk += 1;

        // gpa risk
        Double min = u.getMinGpa();
        if (min != null && gpa < min) risk += 2;

        // exam readiness risk
        if (isNotReady(p.getIeltsStatus())) risk += 1;
        if (isNotReady(p.getGreStatus())) risk += 1;
        if (isNotReady(p.getSopStatus())) risk += 1;

        if (risk >= 4) return "HIGH";
        if (risk >= 2) return "MEDIUM";
        return "LOW";
    }

    private String reason(UserProfile p, University u, String bucket, String acceptance) {
        if ("DREAM".equals(bucket)) {
            return "Strong university fit but competitive; needs strong SOP & exam readiness.";
        }
        if ("TARGET".equals(bucket)) {
            return "Balanced option based on budget/profile with manageable risk.";
        }
        return "Safe pick; high acceptance chances and easier profile match.";
    }

    private boolean isNotReady(String s) {
        if (s == null) return true;
        String x = s.toLowerCase();
        return x.contains("not") || x.contains("pending");
    }

    private String safe(String s) { return s == null ? "" : s; }

    // ------------------------------
    // Shortlist / Lock / Unlock
    // ------------------------------

    public ShortlistResponse shortlist(UUID userId, UUID universityId) {
        // stage gate: must complete onboarding
        UserProfile profile = profileRepo.findById(userId).orElse(null);
        if (profile == null || !profile.isOnboardingCompleted()) {
            throw new RuntimeException("Complete onboarding before shortlisting.");
        }

        University uni = universityRepo.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        Optional<UserUniversity> existing = userUniRepo.findByUserIdAndUniversity_Id(userId, universityId);
        if (existing.isPresent()) {
            return new ShortlistResponse(existing.get().getStatus(), "Already in your list.");
        }

        UserUniversity uu = new UserUniversity();
        uu.setId(UUID.randomUUID());
        uu.setUserId(userId);
        uu.setUniversity(uni);
        uu.setStatus("shortlisted");
        uu.setCreatedAt(OffsetDateTime.now());

        userUniRepo.save(uu);
        stageService.recalculateStage(userId);

        return new ShortlistResponse("shortlisted", "University shortlisted.");
    }

    public ShortlistResponse lock(UUID userId, UUID universityId) {
        UserUniversity uu = userUniRepo.findByUserIdAndUniversity_Id(userId, universityId)
                .orElseThrow(() -> new RuntimeException("Shortlist the university before locking."));

        uu.setStatus("locked");
        uu.setLockedAt(OffsetDateTime.now());
        userUniRepo.save(uu);

        stageService.recalculateStage(userId);

        return new ShortlistResponse("locked", "University locked. Application guidance unlocked.");
    }

    public ShortlistResponse unlock(UUID userId, UUID universityId) {
        UserUniversity uu = userUniRepo.findByUserIdAndUniversity_Id(userId, universityId)
                .orElseThrow(() -> new RuntimeException("University not found in your list"));

        uu.setStatus("shortlisted");
        uu.setLockedAt(null);
        userUniRepo.save(uu);

        stageService.recalculateStage(userId);

        return new ShortlistResponse("shortlisted",
                "University unlocked. Warning: focus may reduce and application stage may lock again.");
    }

    public List<UserUniversity> myShortlist(UUID userId) {
        return userUniRepo.findByUserId(userId);
    }
    public ShortlistResponse lockRecentShortlisted(UUID userId) {
        List<UserUniversity> list = userUniRepo.findByUserId(userId);

        // find most recent shortlisted
        UserUniversity recent = list.stream()
                .filter(x -> "shortlisted".equalsIgnoreCase(x.getStatus()))
                .max(Comparator.comparing(UserUniversity::getCreatedAt))
                .orElse(null);

        if (recent == null) {
            throw new RuntimeException("No shortlisted university found to lock. Shortlist first.");
        }

        recent.setStatus("locked");
        recent.setLockedAt(OffsetDateTime.now());
        userUniRepo.save(recent);

        stageService.recalculateStage(userId);

        return new ShortlistResponse("locked", "Locked your most recently shortlisted university.");
    }

	public void recalculateForUser(UUID userId) {
		// TODO Auto-generated method stub
		
	}

}
