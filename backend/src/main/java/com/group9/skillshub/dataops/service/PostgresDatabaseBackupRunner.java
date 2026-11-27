package com.group9.skillshub.dataops.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PostgresDatabaseBackupRunner implements DatabaseBackupRunner {

    private static final long PROCESS_TIMEOUT_MINUTES = 10;

    private final String pgDumpExecutable;
    private final String pgRestoreExecutable;

    public PostgresDatabaseBackupRunner(
            @Value("${dataops.postgres.pg-dump:pg_dump}") String pgDumpExecutable,
            @Value("${dataops.postgres.pg-restore:pg_restore}") String pgRestoreExecutable) {
        this.pgDumpExecutable = pgDumpExecutable;
        this.pgRestoreExecutable = pgRestoreExecutable;
    }

    @Override
    public void createBackup(String jdbcUrl, String username, String password, Path outputFile) {
        DatabaseConnection connection = DatabaseConnection.fromJdbcUrl(jdbcUrl);
        runCommand(
                List.of(pgDumpExecutable, "--format=custom", "--no-owner", "--no-acl",
                        "--file", outputFile.toString(), "--host", connection.host(),
                        "--port", Integer.toString(connection.port()), "--username", username,
                        "--dbname", connection.database()),
                password, "PostgreSQL backup failed");
    }

    @Override
    public void validateBackup(Path backupFile) {
        runCommand(List.of(pgRestoreExecutable, "--list", backupFile.toString()),
                null, "Backup archive validation failed");
    }

    @Override
    public void restoreBackup(String jdbcUrl, String username, String password, Path backupFile) {
        DatabaseConnection connection = DatabaseConnection.fromJdbcUrl(jdbcUrl);
        runCommand(
                List.of(pgRestoreExecutable, "--clean", "--if-exists", "--no-owner", "--no-acl",
                        "--host", connection.host(), "--port", Integer.toString(connection.port()),
                        "--username", username, "--dbname", connection.database(), backupFile.toString()),
                password, "PostgreSQL restore failed");
    }

    private void runCommand(List<String> command, String password, String failureMessage) {
        Process process;
        try {
            ProcessBuilder builder = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Map<String, String> environment = builder.environment();
            if (password != null && !password.isEmpty()) {
                environment.put("PGPASSWORD", password);
            }
            process = builder.start();
            if (!process.waitFor(PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new IllegalStateException(failureMessage + ": command timed out");
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    failureMessage + ": PostgreSQL client executable could not be started", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failureMessage + ": command was interrupted", exception);
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(failureMessage + " (exit code " + process.exitValue() + ")");
        }
    }

    private record DatabaseConnection(String host, int port, String database) {

        private static DatabaseConnection fromJdbcUrl(String jdbcUrl) {
            String prefix = "jdbc:";
            if (jdbcUrl == null || !jdbcUrl.startsWith(prefix)) {
                throw new IllegalStateException("PostgreSQL backup requires a jdbc:postgresql URL");
            }
            URI uri;
            try {
                uri = URI.create(jdbcUrl.substring(prefix.length()));
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("PostgreSQL JDBC URL is malformed", exception);
            }
            if (!"postgresql".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null || uri.getHost().isBlank()
                    || uri.getPath() == null || uri.getPath().length() < 2) {
                throw new IllegalStateException("PostgreSQL JDBC URL must include host and database");
            }
            String database = uri.getPath().substring(1);
            if (database.contains("/")) {
                throw new IllegalStateException("PostgreSQL JDBC URL contains an invalid database path");
            }
            return new DatabaseConnection(uri.getHost(), uri.getPort() < 0 ? 5432 : uri.getPort(), database);
        }
    }
}
