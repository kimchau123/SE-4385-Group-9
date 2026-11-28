package com.group9.skillshub.organizations.dto;

import java.util.Map;

public record OrganizationSkillCapacityDTO(
    int totalEmployees,
    int employeesWithSkills,
    int employeesWithoutSkills,
    int distinctSkills,
    double skillCoveragePercentage,
    Map<String, Long> proficiencyDistribution
) {}