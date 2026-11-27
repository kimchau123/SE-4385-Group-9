package com.group9.skillshub.dataops.service;

import java.nio.file.Path;

public interface DatabaseBackupRunner {

    void createBackup(String jdbcUrl, String username, String password, Path outputFile);

    void validateBackup(Path backupFile);

    void restoreBackup(String jdbcUrl, String username, String password, Path backupFile);
}
