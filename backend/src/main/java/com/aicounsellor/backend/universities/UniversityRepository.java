package com.aicounsellor.backend.universities;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    List<University> findByCountryIn(List<String> countries);
}
