package com.group9.skillshub.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PersonSkillRequest(
        @NotNull @Positive Integer skillId,
        @NotNull @Positive @Max(5) Integer levelId) {
}
