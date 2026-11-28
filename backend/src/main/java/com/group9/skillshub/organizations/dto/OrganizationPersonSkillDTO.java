package com.group9.skillshub.organizations.dto;

public record OrganizationPersonSkillDTO (int personId, int skillId, String skillName, int skillProficiencyLevelId, String skillProficiencyLevelName, String status)
    {}
