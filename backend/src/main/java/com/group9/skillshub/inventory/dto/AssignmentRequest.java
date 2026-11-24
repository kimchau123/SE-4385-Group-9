package com.group9.skillshub.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignmentRequest(
        @NotNull @Positive Integer personId,
        @NotNull @Positive Integer projectId) {
}
