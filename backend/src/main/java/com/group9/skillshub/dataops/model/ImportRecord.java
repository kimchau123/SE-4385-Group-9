package com.group9.skillshub.dataops.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "data_import")
public class ImportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long importId;

    @Column(nullable = false)
    private int importedCount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    protected ImportRecord() {
    }

    public ImportRecord(int importedCount, String status, String message, Instant createdAt) {
        this.importedCount = importedCount;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public long getImportId() {
        return importId;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
