package com.aicounsellor.backend.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTaskRepository extends JpaRepository<UserTask, UUID> {
    List<UserTask> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
