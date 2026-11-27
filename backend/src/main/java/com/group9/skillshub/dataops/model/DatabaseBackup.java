package com.group9.skillshub.dataops.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "database_backup")
public class DatabaseBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long backupId;

    @Column(nullable = false, unique = true, length = 80)
    private String fileName;

    @Column(nullable = false, length = 64)
    private String checksum;

    @Column(nullable = false)
    private Instant createdAt;

    protected DatabaseBackup() {
    }

    public DatabaseBackup(String fileName, String checksum, Instant createdAt) {
        this.fileName = fileName;
        this.checksum = checksum;
        this.createdAt = createdAt;
    }

    public long getBackupId() {
        return backupId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
