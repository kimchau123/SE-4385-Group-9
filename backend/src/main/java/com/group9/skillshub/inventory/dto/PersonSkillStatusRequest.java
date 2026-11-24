package com.group9.skillshub.inventory.dto;

import com.group9.skillshub.projects.model.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PersonSkillStatusRequest(
        @NotNull @Positive Integer personId,
        @NotNull @Positive Integer skillId,
        @NotNull ApprovalStatus status) {
}
