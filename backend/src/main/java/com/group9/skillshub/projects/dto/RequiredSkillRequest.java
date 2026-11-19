package com.group9.skillshub.projects.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RequiredSkillRequest(
        @NotNull @Positive Integer skillId,
        @NotNull @Positive Integer levelId) {
}
