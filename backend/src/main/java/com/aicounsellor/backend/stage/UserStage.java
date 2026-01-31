package com.aicounsellor.backend.stage;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_stage", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class UserStage {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private int stage = 1;

    private OffsetDateTime updatedAt;
}
