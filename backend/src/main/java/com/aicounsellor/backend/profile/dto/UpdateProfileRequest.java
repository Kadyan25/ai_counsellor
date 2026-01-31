package com.aicounsellor.backend.profile.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    public String educationLevel;
    public String major;
    public Integer gradYear;
    public Double gpa;

    public String intendedDegree;
    public String fieldOfStudy;
    public Integer intakeYear;
    public String[] preferredCountries;

    public Integer budgetPerYear;
    public String fundingPlan;

    public String ieltsStatus;
    public String greStatus;
    public String sopStatus;
}
