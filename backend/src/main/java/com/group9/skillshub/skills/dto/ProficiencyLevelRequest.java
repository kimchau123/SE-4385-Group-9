package com.group9.skillshub.skills.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// The data a client sends to rename a proficiency level.
public record ProficiencyLevelRequest(
        @NotBlank @Size(max = 50) String skillProficiencyLevelName) {
}
