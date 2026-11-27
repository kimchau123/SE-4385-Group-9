package com.group9.skillshub.dataops.service;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.dataops.dto.ImportResult;
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
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataOpsServiceImplTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PersonSkillRepository personSkillRepository;
    @Mock
    private SkillProficiencyLevelRepository levelRepository;
    @Mock
    private ImportRecordRepository importRecordRepository;
    @Mock
    private DatabaseBackupRepository backupRepository;
    @Mock
    private DatabaseBackupRunner backupRunner;

    @TempDir
    Path tempDirectory;

    private DataOpsServiceImpl service;

    @BeforeEach
    void setUp() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl("jdbc:postgresql://localhost:5432/skillshub");
        dataSourceProperties.setUsername("test-user");
        dataSourceProperties.setPassword("test-password");
        service = new DataOpsServiceImpl(
                skillRepository, personRepository, organizationRepository, personSkillRepository,
                levelRepository, importRecordRepository, backupRepository, backupRunner,
                dataSourceProperties, tempDirectory);
    }

    @Test
    void validatesCsvHeadersAndRowsAndRejectsDuplicateNames() {
        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(false);
        String validCsv = """
                skillName,skillDescription,skillType
                Java,"JVM language, widely used",TECHNOLOGY
                """;
        var result = service.validateImportedData(validCsv.getBytes(StandardCharsets.UTF_8));

        assertTrue(result.valid());
        assertEquals(1, result.rowCount());
        assertTrue(result.errors().isEmpty());

        String duplicateCsv = """
                skillName,skillDescription,skillType
                Java,Programming language,TECHNOLOGY
                JAVA,Another programming language,TECHNOLOGY
                """;
        var duplicateResult = service.validateImportedData(duplicateCsv.getBytes(StandardCharsets.UTF_8));
        assertFalse(duplicateResult.valid());
        assertTrue(duplicateResult.errors().stream().anyMatch(error -> error.contains("duplicated")));
    }

    @Test
    void rejectsEmptyMalformedOversizedAndInvalidCsvFiles() {
        assertFalse(service.validateImportedData(new byte[0]).valid());
        assertFalse(service.validateImportedData(
                "skillName,skillDescription,skillType\n".getBytes(StandardCharsets.UTF_8)).valid());
        assertFalse(service.validateImportedData(
                "wrong,header,values\nJava,description,OTHER\n".getBytes(StandardCharsets.UTF_8)).valid());
        assertFalse(service.validateImportedData(new byte[5 * 1024 * 1024 + 1]).valid());
        assertFalse(service.validateImportedData(new byte[]{(byte) 0xC3, (byte) 0x28}).valid());
        assertFalse(service.validateImportedData("""
                skillName,skillDescription,skillType
                Java,short,UNKNOWN
                """.getBytes(StandardCharsets.UTF_8)).valid());
    }

    @Test
    void importPersistsRequestedSkillsAndImportHistory() {
        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(false);
        when(skillRepository.existsBySkillNameIgnoreCase("SQL")).thenReturn(false);
        when(skillRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(importRecordRepository.save(any(ImportRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        String csv = """
                skillName,skillDescription,skillType
                Java,JVM programming language,TECHNOLOGY
                SQL,Relational query language,KNOWLEDGE
                """;

        ImportResult result = service.importSkillsCatalog(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, result.importedCount());
        assertEquals("IMPORTED", result.status());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Skill>> captor =
                (ArgumentCaptor<List<Skill>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(skillRepository).saveAll(captor.capture());
        assertEquals(List.of(ApprovalStatus.REQUESTED, ApprovalStatus.REQUESTED),
                captor.getValue().stream().map(Skill::getStatus).toList());
        verify(importRecordRepository).save(any(ImportRecord.class));
    }

    @Test
    void invalidImportDoesNotPersistAnySkills() {
        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(true);
        String csv = """
                skillName,skillDescription,skillType
                Java,JVM programming language,TECHNOLOGY
                """;

        assertThrows(IllegalArgumentException.class,
                () -> service.importSkillsCatalog(csv.getBytes(StandardCharsets.UTF_8)));
        verify(skillRepository, never()).saveAll(any());
        verify(importRecordRepository, never()).save(any(ImportRecord.class));
    }

    @Test
    void retrievesImportHistoryAndReturnsNotFoundForUnknownId() {
        ImportRecord record = new ImportRecord(3, "IMPORTED", "Imported 3 skills", Instant.now());
        when(importRecordRepository.findById(9L)).thenReturn(Optional.of(record));
        assertEquals(3, service.getImportedResults(9).importedCount());
        when(importRecordRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getImportedResults(404));
    }

    @Test
    void exportsOrganizationInventoryAsCsvWithFormulaSafeValues() throws Exception {
        when(organizationRepository.existsById(2)).thenReturn(true);
        Person person = new Person();
        person.setPersonId(3);
        person.setPersonName("=HYPERLINK(\"url\")");
        when(personRepository.findByMemberOfOrganizationId(2)).thenReturn(List.of(person));
        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(3);
        personSkill.setSkillId(5);
        personSkill.setSkillProficiencyLevelId(1);
        personSkill.setStatus(ApprovalStatus.APPROVED);
        when(personSkillRepository.findByPersonId(3)).thenReturn(List.of(personSkill));
        Skill skill = new Skill();
        skill.setSkillId(5);
        skill.setSkillName("Java");
        when(skillRepository.findById(5)).thenReturn(Optional.of(skill));
        SkillProficiencyLevel level = new SkillProficiencyLevel();
        level.setSkillProficiencyLevelId(1);
        level.setSkillProficiencyLevelName("Beginner");
        when(levelRepository.findById(1)).thenReturn(Optional.of(level));

        byte[] report = service.exportInventoryReport(2);
        try (CSVParser parser = CSVParser.parse(
                new String(report, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get())) {
            CSVRecord row = parser.getRecords().get(0);
            assertEquals("2", row.get("organizationId"));
            assertEquals("'=HYPERLINK(\"url\")", row.get("personName"));
            assertEquals("Java", row.get("skillName"));
            assertEquals("Beginner", row.get("skillProficiencyLevelName"));
        }
    }

    @Test
    void exportsNotFoundForUnknownOrganization() {
        when(organizationRepository.existsById(404)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.exportInventoryReport(404));
    }

    @Test
    void createsAndRestoresChecksumVerifiedBackup() throws Exception {
        doAnswer(invocation -> {
            Path output = invocation.getArgument(3);
            Files.writeString(output, "postgres archive");
            return null;
        }).when(backupRunner).createBackup(any(), any(), any(), any());
        when(backupRepository.save(any(DatabaseBackup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createBackup();

        ArgumentCaptor<DatabaseBackup> captor = ArgumentCaptor.forClass(DatabaseBackup.class);
        verify(backupRepository).save(captor.capture());
        DatabaseBackup saved = captor.getValue();
        Path archive = tempDirectory.resolve(saved.getFileName());
        assertTrue(Files.isRegularFile(archive));
        when(backupRepository.findById(1L)).thenReturn(Optional.of(saved));

        service.restoreBackup(1);

        verify(backupRunner).validateBackup(archive);
        verify(backupRunner).restoreBackup(
                "jdbc:postgresql://localhost:5432/skillshub", "test-user", "test-password", archive);
    }

    @Test
    void failedBackupCreationRemovesPartialArchive() throws Exception {
        doAnswer(invocation -> {
            Path output = invocation.getArgument(3);
            Files.writeString(output, "partial archive");
            throw new IllegalStateException("pg_dump failed");
        }).when(backupRunner).createBackup(any(), any(), any(), any());

        assertThrows(IllegalStateException.class, service::createBackup);

        try (var files = Files.list(tempDirectory)) {
            assertEquals(0, files.count());
        }
        verify(backupRepository, never()).save(any(DatabaseBackup.class));
    }

    @Test
    void refusesToRestoreMissingOrModifiedBackupArchive() throws Exception {
        Path archive = tempDirectory.resolve("00000000-0000-0000-0000-000000000001.dump");
        Files.writeString(archive, "modified archive");
        DatabaseBackup backup = new DatabaseBackup(
                archive.getFileName().toString(), "incorrect-checksum", Instant.now());
        when(backupRepository.findById(1L)).thenReturn(Optional.of(backup));

        assertThrows(IllegalStateException.class, () -> service.restoreBackup(1));
        verify(backupRunner, never()).restoreBackup(any(), any(), any(), any());
    }

}
