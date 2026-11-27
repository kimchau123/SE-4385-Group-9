package com.group9.skillshub.dataops.service;

import com.group9.skillshub.dataops.dto.BackupResult;
import com.group9.skillshub.dataops.dto.ImportResult;
import com.group9.skillshub.dataops.dto.ImportValidationResult;

public interface DataOpsService {

    ImportValidationResult validateImportedData(byte[] fileBytes);

    ImportResult importSkillsCatalog(byte[] fileBytes);

    ImportResult getImportedResults(long importId);

    byte[] exportInventoryReport(int organizationId);

    BackupResult createBackup();

    void restoreBackup(long backupId);
}
