package com.group9.skillshub.dataops.service;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.dataops.dto.BackupResult;
import com.group9.skillshub.dataops.dto.ImportResult;
import com.group9.skillshub.dataops.dto.ImportValidationResult;
import com.group9.skillshub.dataops.model.DatabaseBackup;
import com.group9.skillshub.dataops.model.ImportRecord;
import com.group9.skillshub.dataops.repository.DatabaseBackupRepository;
import com.group9.skillshub.dataops.repository.ImportRecordRepository;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.organizations.repository.PersonRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DataOpsServiceImpl implements DataOpsService {

    private static final Set<String> CSV_HEADERS =
            Set.of("skillName", "skillDescription", "skillType");
    private static final int MAX_FILE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_VALIDATION_ERRORS = 100;

    private final SkillRepository skillRepository;
    private final PersonRepository personRepository;
    private final OrganizationRepository organizationRepository;
    private final PersonSkillRepository personSkillRepository;
    private final SkillProficiencyLevelRepository levelRepository;
    private final ImportRecordRepository importRecordRepository;
    private final DatabaseBackupRepository backupRepository;
    private final DatabaseBackupRunner backupRunner;
    private final DataSourceProperties dataSourceProperties;
    private final Path backupDirectory;

    public DataOpsServiceImpl(
            SkillRepository skillRepository,
            PersonRepository personRepository,
            OrganizationRepository organizationRepository,
            PersonSkillRepository personSkillRepository,
            SkillProficiencyLevelRepository levelRepository,
            ImportRecordRepository importRecordRepository,
            DatabaseBackupRepository backupRepository,
            DatabaseBackupRunner backupRunner,
            DataSourceProperties dataSourceProperties,
            @Value("${dataops.backup-directory:./backups}") Path backupDirectory) {
        this.skillRepository = skillRepository;
        this.personRepository = personRepository;
        this.organizationRepository = organizationRepository;
        this.personSkillRepository = personSkillRepository;
        this.levelRepository = levelRepository;
        this.importRecordRepository = importRecordRepository;
        this.backupRepository = backupRepository;
        this.backupRunner = backupRunner;
        this.dataSourceProperties = dataSourceProperties;
        this.backupDirectory = backupDirectory.toAbsolutePath().normalize();
    }

    @Override
    @Transactional(readOnly = true)
    public ImportValidationResult validateImportedData(byte[] fileBytes) {
        ParsedImport parsedImport = parseImport(fileBytes);
        return new ImportValidationResult(
                parsedImport.errors().isEmpty(), parsedImport.rowCount(), parsedImport.errors());
    }

    @Override
    @Transactional
    public ImportResult importSkillsCatalog(byte[] fileBytes) {
        ParsedImport parsedImport = parseImport(fileBytes);
        if (!parsedImport.errors().isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", parsedImport.errors()));
        }
        List<Skill> savedSkills = skillRepository.saveAll(parsedImport.skills());
        Instant createdAt = Instant.now();
        ImportRecord record = importRecordRepository.save(new ImportRecord(
                savedSkills.size(), "IMPORTED", "Imported " + savedSkills.size() + " skills", createdAt));
        return toImportResult(record);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportResult getImportedResults(long importId) {
        ImportRecord record = importRecordRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException("Import " + importId + " was not found"));
        return toImportResult(record);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInventoryReport(int organizationId) {
        if (organizationId <= 0) {
            throw new IllegalArgumentException("organizationId must be positive");
        }
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization " + organizationId + " was not found");
        }
        List<Person> people = personRepository.findByMemberOfOrganizationId(organizationId);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("organizationId", "personId", "personName", "skillId", "skillName",
                        "skillProficiencyLevelId", "skillProficiencyLevelName", "status")
                .get();
        try (CSVPrinter printer =
                     new CSVPrinter(new OutputStreamWriter(output, StandardCharsets.UTF_8), format)) {
            for (Person person : people) {
                for (PersonSkill personSkill : personSkillRepository.findByPersonId(person.getPersonId())) {
                    Skill skill = skillRepository.findById(personSkill.getSkillId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Skill " + personSkill.getSkillId() + " was not found"));
                    SkillProficiencyLevel level = levelRepository
                            .findById(personSkill.getSkillProficiencyLevelId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Proficiency level " + personSkill.getSkillProficiencyLevelId()
                                            + " was not found"));
                    printer.printRecord(organizationId, person.getPersonId(),
                            csvSafe(person.getPersonName()), personSkill.getSkillId(),
                            csvSafe(skill.getSkillName()), personSkill.getSkillProficiencyLevelId(),
                            csvSafe(level.getSkillProficiencyLevelName()), personSkill.getStatus());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Inventory report could not be generated", exception);
        }
        return output.toByteArray();
    }

    @Override
    public BackupResult createBackup() {
        String backupName = UUID.randomUUID().toString();
        Path temporaryFile = backupDirectory.resolve(backupName + ".partial");
        Path archiveFile = backupDirectory.resolve(backupName + ".dump");
        boolean backupRecordSaved = false;
        try {
            createPrivateBackupDirectory();
            backupRunner.createBackup(dataSourceProperties.getUrl(),
                    dataSourceProperties.getUsername(), dataSourceProperties.getPassword(), temporaryFile);
            if (!Files.isRegularFile(temporaryFile, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(temporaryFile) == 0) {
                throw new IllegalStateException("PostgreSQL backup command produced no archive");
            }
            setOwnerOnlyPermissions(temporaryFile, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            String checksum = checksum(temporaryFile);
            moveIntoPlace(temporaryFile, archiveFile);
            Instant createdAt = Instant.now();
            DatabaseBackup saved = backupRepository.save(
                    new DatabaseBackup(archiveFile.getFileName().toString(), checksum, createdAt));
            backupRecordSaved = true;
            return new BackupResult(saved.getBackupId(), saved.getCreatedAt());
        } catch (IOException exception) {
            throw new IllegalStateException("Database backup file could not be created", exception);
        } finally {
            deleteIfExists(temporaryFile);
            if (!backupRecordSaved) {
                deleteIfExists(archiveFile);
            }
        }
    }

    @Override
    public void restoreBackup(long backupId) {
        DatabaseBackup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new ResourceNotFoundException("Backup " + backupId + " was not found"));
        Path backupFile = backupDirectory.resolve(backup.getFileName()).normalize();
        if (!backupFile.getParent().equals(backupDirectory)
                || !backup.getFileName().matches("[0-9a-fA-F-]{36}\\.dump")
                || !Files.isRegularFile(backupFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResourceNotFoundException("Backup archive " + backupId + " was not found");
        }
        try {
            if (!MessageDigest.isEqual(
                    backup.getChecksum().getBytes(StandardCharsets.US_ASCII),
                    checksum(backupFile).getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalStateException("Backup archive checksum does not match");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Backup archive could not be verified", exception);
        }
        backupRunner.validateBackup(backupFile);
        backupRunner.restoreBackup(dataSourceProperties.getUrl(),
                dataSourceProperties.getUsername(), dataSourceProperties.getPassword(), backupFile);
    }

    private ParsedImport parseImport(byte[] fileBytes) {
        List<String> errors = new ArrayList<>();
        if (fileBytes == null || fileBytes.length == 0) {
            return new ParsedImport(List.of(), 0, List.of("CSV file is empty"));
        }
        if (fileBytes.length > MAX_FILE_BYTES) {
            return new ParsedImport(List.of(), 0, List.of("CSV file must not exceed 5 MB"));
        }

        List<Skill> skills = new ArrayList<>();
        Set<String> namesInFile = new HashSet<>();
        int rowCount = 0;
        try {
            String csv = decodeUtf8(fileBytes);
            if (!csv.isEmpty() && csv.charAt(0) == '\uFEFF') {
                csv = csv.substring(1);
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .get();
            try (CSVParser parser = CSVParser.parse(csv, format)) {
                if (!parser.getHeaderMap().keySet().equals(CSV_HEADERS)) {
                    return new ParsedImport(List.of(), 0, List.of(
                            "CSV headers must be exactly skillName, skillDescription, skillType"));
                }
                for (CSVRecord record : parser) {
                    rowCount++;
                    if (errors.size() >= MAX_VALIDATION_ERRORS) {
                        continue;
                    }
                    parseRecord(record, skills, namesInFile, errors);
                }
            }
        } catch (CharacterCodingException exception) {
            errors.add("CSV file must be valid UTF-8");
        } catch (IOException | IllegalArgumentException exception) {
            errors.add("CSV file is malformed: " + exception.getMessage());
        }

        if (rowCount == 0 && errors.isEmpty()) {
            errors.add("CSV file has no data rows");
        }
        return new ParsedImport(List.copyOf(skills), rowCount, List.copyOf(errors));
    }

    private void parseRecord(
            CSVRecord record, List<Skill> skills, Set<String> namesInFile, List<String> errors) {
        String name = record.get("skillName");
        String description = record.get("skillDescription");
        String type = record.get("skillType");
        List<String> rowErrors = new ArrayList<>();
        long rowNumber = record.getRecordNumber() + 1;
        if (name.isBlank() || name.length() > 50) {
            rowErrors.add("skillName must contain 1 to 50 characters");
        }
        if (!description.isBlank() && (description.length() < 10 || description.length() > 500)) {
            rowErrors.add("skillDescription must be empty or contain 10 to 500 characters");
        }
        SkillType skillType = null;
        try {
            skillType = SkillType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            rowErrors.add("skillType must be KNOWLEDGE, EXPERIENCE, TECHNOLOGY, or OTHER");
        }
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (!name.isBlank()) {
            if (!namesInFile.add(normalizedName)) {
                rowErrors.add("skillName is duplicated in this file");
            } else if (skillRepository.existsBySkillNameIgnoreCase(name)) {
                rowErrors.add("skillName already exists in the catalog");
            }
        }
        if (rowErrors.isEmpty()) {
            Skill skill = new Skill();
            skill.setSkillName(name);
            skill.setSkillDescription(description.isBlank() ? null : description);
            skill.setSkillType(skillType);
            skill.setStatus(ApprovalStatus.REQUESTED);
            skills.add(skill);
        } else {
            rowErrors.forEach(error -> errors.add("Row " + rowNumber + ": " + error));
        }
    }

    private ImportResult toImportResult(ImportRecord record) {
        return new ImportResult(record.getImportId(), record.getImportedCount(),
                record.getStatus(), record.getMessage(), record.getCreatedAt());
    }

    private String decodeUtf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }

    private String csvSafe(String value) {
        if (value != null && !value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    private String checksum(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
        try (DigestInputStream input = new DigestInputStream(Files.newInputStream(file), digest)) {
            input.transferTo(ByteArrayOutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private void createPrivateBackupDirectory() throws IOException {
        Files.createDirectories(backupDirectory);
        setOwnerOnlyPermissions(backupDirectory, EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE));
    }

    private void setOwnerOnlyPermissions(Path path, Set<PosixFilePermission> permissions) throws IOException {
        if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(path, permissions);
        }
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Temporary backup file could not be removed", exception);
        }
    }

    private record ParsedImport(List<Skill> skills, int rowCount, List<String> errors) {
    }
}
