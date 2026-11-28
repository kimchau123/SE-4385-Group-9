package com.group9.skillshub.skills.repository;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the generated repository queries against a real in-memory H2 database.
 */
@DataJpaTest
class SkillsRepositoriesTest {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillProficiencyLevelRepository levelRepository;

    private Skill java;
    private Skill dataStorage;

    @BeforeEach
    void saveSkills() {
        java = save("Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED);
        dataStorage = save("Data Storage", SkillType.TECHNOLOGY, ApprovalStatus.REQUESTED);
        save("Data Processing", SkillType.EXPERIENCE, ApprovalStatus.APPROVED);
    }

    private Skill save(String name, SkillType type, ApprovalStatus status) {
        Skill skill = new Skill();
        skill.setSkillName(name);
        skill.setSkillType(type);
        skill.setStatus(status);
        return skillRepository.save(skill);
    }

    private static List<String> names(List<Skill> skills) {
        return skills.stream().map(Skill::getSkillName).sorted().toList();
    }

    @Test
    void nameSearchIsPartialAndCaseInsensitive() {
        assertEquals(List.of("Data Processing", "Data Storage"),
                names(skillRepository.findBySkillNameContainingIgnoreCase("DAT")));
        assertEquals(List.of("Java"), names(skillRepository.findBySkillNameContainingIgnoreCase("av")));
    }

    @Test
    void findsByTypeAndStatus() {
        assertEquals(List.of("Data Storage", "Java"),
                names(skillRepository.findBySkillType(SkillType.TECHNOLOGY)));
        assertEquals(List.of("Data Processing", "Java"),
                names(skillRepository.findByStatus(ApprovalStatus.APPROVED)));
        assertEquals(List.of("Data Storage"),
                names(skillRepository.findBySkillTypeAndStatus(SkillType.TECHNOLOGY, ApprovalStatus.REQUESTED)));
    }

    @Test
    void duplicateChecksIgnoreCaseAndExcludeOwnId() {
        assertTrue(skillRepository.existsBySkillNameIgnoreCase("jAVA"));
        assertFalse(skillRepository.existsBySkillNameIgnoreCase("JavaScript"));
        assertFalse(skillRepository.existsBySkillNameIgnoreCaseAndSkillIdNot("java", java.getSkillId()));
        assertTrue(skillRepository.existsBySkillNameIgnoreCaseAndSkillIdNot("java", dataStorage.getSkillId()));
    }

    @Test
    void proficiencyLevelDuplicateCheckIgnoresCaseAndExcludesOwnId() {
        SkillProficiencyLevel beginner = new SkillProficiencyLevel();
        beginner.setSkillProficiencyLevelName("Beginner");
        beginner = levelRepository.save(beginner);
        SkillProficiencyLevel expert = new SkillProficiencyLevel();
        expert.setSkillProficiencyLevelName("Expert");
        expert = levelRepository.save(expert);

        assertFalse(levelRepository.existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot(
                "beginner", beginner.getSkillProficiencyLevelId()));
        assertTrue(levelRepository.existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot(
                "BEGINNER", expert.getSkillProficiencyLevelId()));
    }
}
