package com.group9.skillshub.dataops.repository;

import com.group9.skillshub.dataops.model.DatabaseBackup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseBackupRepository extends JpaRepository<DatabaseBackup, Long> {
}
