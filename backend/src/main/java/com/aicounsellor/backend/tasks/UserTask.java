package com.aicounsellor.backend.tasks;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_tasks", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class UserTask {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    private String title;

    private String status; // pending/done
    private String source; // ai/manual

    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}
