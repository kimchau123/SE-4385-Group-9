package com.group9.skillshub.dataops.dto;

import java.time.Instant;

public record ImportResult(long importId, int importedCount, String status, String message, Instant createdAt) {
}
