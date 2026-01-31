package com.aicounsellor.backend.stage;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStageRepository extends JpaRepository<UserStage, UUID> {}
