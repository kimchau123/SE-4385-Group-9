package com.group9.skillshub.skills.dto;

import com.group9.skillshub.skills.model.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// The data a client sends to create or edit a skill, rejected with 400 if a field breaks its length or required rule.
// It has no status or skillId on purpose, so clients can't approve their own skill or change a skill's id.
public record SkillRequest(
        @NotBlank @Size(max = 50) String skillName,
        @Size(min = 10, max = 500) String skillDescription,
        @NotNull SkillType skillType) {

    // Trims spaces from the name and description, and turns a blank description into null, before validation runs.
    public SkillRequest {
        if (skillName != null) {
            skillName = skillName.trim();
        }
        if (skillDescription != null) {
            skillDescription = skillDescription.isBlank() ? null : skillDescription.trim();
        }
    }
}
