package com.aicounsellor.backend.universities;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUniversityRepository extends JpaRepository<UserUniversity, UUID> {

    List<UserUniversity> findByUserId(UUID userId);

    Optional<UserUniversity> findByUserIdAndUniversity_Id(UUID userId, UUID universityId);

    boolean existsByUserIdAndStatus(UUID userId, String status);
}
