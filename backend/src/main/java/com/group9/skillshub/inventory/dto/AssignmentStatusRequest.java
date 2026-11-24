package com.group9.skillshub.inventory.dto;

import com.group9.skillshub.projects.model.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignmentStatusRequest(
        @NotNull @Positive Integer personId,
        @NotNull @Positive Integer projectId,
        @NotNull ApprovalStatus status) {
}
