package com.group9.skillshub.dataops.dto;

import java.time.Instant;

public record BackupResult(long backupId, Instant createdAt) {
}
