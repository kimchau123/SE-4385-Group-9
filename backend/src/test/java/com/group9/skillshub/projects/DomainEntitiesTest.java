package com.group9.skillshub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.model.PersonSkillId;
import com.group9.skillshub.organization.model.Organization;
import com.group9.skillshub.organization.model.Person;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class DomainEntitiesTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @Autowired
    private TestEntityManager em;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static Skill skill(String name, String description) {
        Skill skill = new Skill();
        skill.setSkillName(name);
        skill.setSkillDescription(description);
        skill.setSkillType(SkillType.TECHNOLOGY);
        return skill;
    }

    @Test
    void skillNameBoundaries() { // TC-WP-01 / TC-WP-02
        String description = "a".repeat(10);
        assertTrue(validator.validate(skill("a", description)).isEmpty());
        assertTrue(validator.validate(skill("a".repeat(50), description)).isEmpty());
        assertFalse(validator.validate(skill("", description)).isEmpty());
        assertFalse(validator.validate(skill("a".repeat(51), description)).isEmpty());
    }

    @Test
    void skillDescriptionBoundaries() {
        assertTrue(validator.validate(skill("Java", "a".repeat(10))).isEmpty());
        assertTrue(validator.validate(skill("Java", "a".repeat(500))).isEmpty());
        assertFalse(validator.validate(skill("Java", "a".repeat(9))).isEmpty());
        assertFalse(validator.validate(skill("Java", "a".repeat(501))).isEmpty());
    }

    @Test
    void newSkillIsPersistedAsRequested() {
        Skill saved = em.persistFlushFind(skill("Java Programming", "Object-oriented language"));
        assertTrue(saved.getSkillId() > 0);
        assertEquals(ApprovalStatus.REQUESTED, saved.getStatus());
        assertEquals(SkillType.TECHNOLOGY, saved.getSkillType());
    }

    @Test
    void personAdminFlagRoundTripsAsInt() { // TC-OPS-05
        Person person = new Person();
        person.setPersonName("Ada");
        person.setAdmin(true);
        Person saved = em.persistFlushFind(person);
        assertTrue(saved.isAdmin());

        Person regular = new Person();
        regular.setPersonName("Bob");
        assertFalse(em.persistFlushFind(regular).isAdmin());
    }

    @Test
    void organizationPersists() {
        Organization org = new Organization();
        org.setOrganizationName("Engineering");
        assertTrue(em.persistFlushFind(org).getOrganizationId() > 0);
    }

    @Test
    void proficiencyLevelPersists() {
        SkillProficiencyLevel level = new SkillProficiencyLevel();
        level.setSkillProficiencyLevelName("Test Level");
        assertNotNull(em.persistFlushFind(level).getSkillProficiencyLevelName());
    }

    @Test
    void personSkillUsesCompositeKeyAndDefaultsToRequested() { // TC-RM-01
        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(1);
        personSkill.setSkillId(2);
        personSkill.setSkillProficiencyLevelId(3);
        em.persistAndFlush(personSkill);
        em.clear();

        PersonSkill found = em.find(PersonSkill.class, new PersonSkillId(1, 2));
        assertNotNull(found);
        assertEquals(3, found.getSkillProficiencyLevelId());
        assertEquals(ApprovalStatus.REQUESTED, found.getStatus());
    }
}
