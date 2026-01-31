package com.aicounsellor.backend.universities;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "universities", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class University {

    @Id
    private UUID id;

    private String name;
    private String country;

    private String degree;
    private String field;

    private int yearlyCostUsd;

    private Double minGpa;

    private String difficulty; // low/medium/high

    private OffsetDateTime createdAt;
}
