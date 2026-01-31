package com.aicounsellor.backend.ai;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ai_messages")
public class AiMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role; // user | assistant

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
