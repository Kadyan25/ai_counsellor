package com.aicounsellor.backend.ai;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {

    List<AiMessage> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
