package com.group9.skillshub.dataops.repository;

import com.group9.skillshub.dataops.model.DatabaseBackup;
import com.group9.skillshub.dataops.model.ImportRecord;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.organizations.model.Organization;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.organizations.repository.PersonRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class DataOpsRepositoriesTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillProficiencyLevelRepository levelRepository;

    @Autowired
    private PersonSkillRepository personSkillRepository;

    @Autowired
    private ImportRecordRepository importRecordRepository;

    @Autowired
    private DatabaseBackupRepository backupRepository;

    @Test
    void persistsImportBackupAndInventoryReportData() {
        Organization organization = new Organization();
        organization.setOrganizationName("Engineering");
        organization = organizationRepository.save(organization);
        Person person = new Person();
        person.setPersonName("Taylor");
        person.setMemberOfOrganizationId(organization.getOrganizationId());
        person = personRepository.save(person);
        Skill skill = new Skill();
        skill.setSkillName("Java");
        skill.setSkillType(SkillType.TECHNOLOGY);
        skill.setStatus(ApprovalStatus.REQUESTED);
        skill = skillRepository.save(skill);
        SkillProficiencyLevel level = new SkillProficiencyLevel();
        level.setSkillProficiencyLevelName("Beginner");
        level = levelRepository.save(level);
        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(person.getPersonId());
        personSkill.setSkillId(skill.getSkillId());
        personSkill.setSkillProficiencyLevelId(level.getSkillProficiencyLevelId());
        personSkill.setStatus(ApprovalStatus.APPROVED);
        personSkillRepository.save(personSkill);

        ImportRecord importRecord = importRecordRepository.save(
                new ImportRecord(1, "IMPORTED", "Imported one skill", Instant.now()));
        DatabaseBackup backup = backupRepository.save(
                new DatabaseBackup("00000000-0000-0000-0000-000000000001.dump", "checksum", Instant.now()));

        assertTrue(skillRepository.existsBySkillNameIgnoreCase("JAVA"));
        assertEquals(1, personRepository.findByMemberOfOrganizationId(
                organization.getOrganizationId()).size());
        assertEquals(1, personSkillRepository.findByPersonId(person.getPersonId()).size());
        assertEquals(1, personSkillRepository.findBySkillIdAndSkillProficiencyLevelId(
                skill.getSkillId(), level.getSkillProficiencyLevelId()).size());
        assertEquals(importRecord.getImportId(), importRecordRepository.findById(
                importRecord.getImportId()).orElseThrow().getImportId());
        assertEquals(backup.getFileName(), backupRepository.findById(
                backup.getBackupId()).orElseThrow().getFileName());
    }
}
