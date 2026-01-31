package com.aicounsellor.backend.universities;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_universities", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class UserUniversity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    private String status; // shortlisted/locked

    private OffsetDateTime lockedAt;

    private OffsetDateTime createdAt;
}
