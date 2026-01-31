package com.aicounsellor.backend.profile;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profile", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private String educationLevel;
    private String major;

    private Integer gradYear;
    private Double gpa;

    private String intendedDegree;
    private String fieldOfStudy;
    private Integer intakeYear;

    @Column(name = "preferred_countries", columnDefinition = "text[]")
    private String[] preferredCountries;

    private Integer budgetPerYear;
    private String fundingPlan;

    private String ieltsStatus;
    private String greStatus;
    private String sopStatus;

    private boolean onboardingCompleted = false;

    private OffsetDateTime updatedAt;
}
