package com.group9.skillshub.dataops.repository;

import com.group9.skillshub.dataops.model.ImportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRecordRepository extends JpaRepository<ImportRecord, Long> {
}
