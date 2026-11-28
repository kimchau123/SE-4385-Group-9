package com.group9.skillshub.skills.service;

import com.group9.skillshub.common.ResourceConflictException;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.ProjectSkillRequiredRepository;
import com.group9.skillshub.skills.dto.ProficiencyLevelRequest;
import com.group9.skillshub.skills.dto.SkillRequest;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit tests for the catalog rules, using mocked repositories instead of a database.
@ExtendWith(MockitoExtension.class)
class SkillsCatalogServiceImplTest {

    private static final String DESCRIPTION = "A general-purpose programming language.";

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillProficiencyLevelRepository levelRepository;

    @Mock
    private PersonSkillRepository personSkillRepository;

    @Mock
    private ProjectSkillRequiredRepository projectSkillRequiredRepository;

    private SkillsCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SkillsCatalogServiceImpl(
                skillRepository, levelRepository, personSkillRepository, projectSkillRequiredRepository);
    }

    private static Skill skill(int id, String name, SkillType type, ApprovalStatus status) {
        Skill skill = new Skill();
        skill.setSkillId(id);
        skill.setSkillName(name);
        skill.setSkillType(type);
        skill.setStatus(status);
        return skill;
    }

    private void saveReturnsItsArgument() {
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // TC-WP-01, TC-WP-02

    @Test
    void addSkillSavesNewSkillAsRequested() {
        when(skillRepository.existsBySkillNameIgnoreCase("Python")).thenReturn(false);
        saveReturnsItsArgument();

        Skill created = service.addSkill(new SkillRequest("Python", DESCRIPTION, SkillType.TECHNOLOGY));

        assertEquals("Python", created.getSkillName());
        assertEquals(DESCRIPTION, created.getSkillDescription());
        assertEquals(SkillType.TECHNOLOGY, created.getSkillType());
        assertEquals(ApprovalStatus.REQUESTED, created.getStatus());
    }

    @Test
    void addSkillRejectsDuplicateNameWithConflict() {
        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> service.addSkill(new SkillRequest("Java", null, SkillType.TECHNOLOGY)));
        verify(skillRepository, never()).save(any());
    }

    @Test
    void addSkillTrimsNameAndTreatsBlankDescriptionAsNone() {
        when(skillRepository.existsBySkillNameIgnoreCase("Python")).thenReturn(false);
        saveReturnsItsArgument();

        Skill created = service.addSkill(new SkillRequest("  Python  ", "   ", SkillType.TECHNOLOGY));

        verify(skillRepository).existsBySkillNameIgnoreCase("Python");
        assertEquals("Python", created.getSkillName());
        assertNull(created.getSkillDescription());
    }

    @Test
    void getSkillByIdThrowsNotFoundForMissingId() {
        when(skillRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSkillById(99));
    }

    @Test
    void updateSkillChangesFieldsButKeepsStatus() {
        Skill existing = skill(4, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED);
        when(skillRepository.findById(4)).thenReturn(Optional.of(existing));
        when(skillRepository.existsBySkillNameIgnoreCaseAndSkillIdNot("Java 21", 4)).thenReturn(false);
        saveReturnsItsArgument();

        Skill updated = service.updateSkill(4, new SkillRequest("Java 21", DESCRIPTION, SkillType.KNOWLEDGE));

        assertEquals("Java 21", updated.getSkillName());
        assertEquals(DESCRIPTION, updated.getSkillDescription());
        assertEquals(SkillType.KNOWLEDGE, updated.getSkillType());
        assertEquals(ApprovalStatus.APPROVED, updated.getStatus());
    }

    @Test
    void updateSkillRejectsNameUsedByAnotherSkill() {
        when(skillRepository.findById(4)).thenReturn(
                Optional.of(skill(4, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED)));
        when(skillRepository.existsBySkillNameIgnoreCaseAndSkillIdNot("C++", 4)).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> service.updateSkill(4, new SkillRequest("C++", null, SkillType.TECHNOLOGY)));
        verify(skillRepository, never()).save(any());
    }

    @Test
    void updateSkillThrowsNotFoundForMissingId() {
        when(skillRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateSkill(99, new SkillRequest("Java", null, SkillType.TECHNOLOGY)));
    }

    // TC-WP-03

    @Test
    void searchWithBlankQueryReturnsAllSkills() {
        List<Skill> all = List.of(skill(1, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED));
        when(skillRepository.findAll()).thenReturn(all);

        assertEquals(all, service.searchSkills("   "));
        assertEquals(all, service.searchSkills(null));
    }

    @Test
    void searchTrimsQueryAndSearchesByName() {
        List<Skill> matches = List.of(skill(10, "Data Processing", SkillType.EXPERIENCE, ApprovalStatus.APPROVED));
        when(skillRepository.findBySkillNameContainingIgnoreCase("data")).thenReturn(matches);

        assertEquals(matches, service.searchSkills("  data "));
    }

    @Test
    void filterUsesTypeAndStatusQueryWhenBothGiven() {
        List<Skill> matches = List.of(skill(1, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED));
        when(skillRepository.findBySkillTypeAndStatus(SkillType.TECHNOLOGY, ApprovalStatus.APPROVED))
                .thenReturn(matches);

        assertEquals(matches, service.filterSkills(null, SkillType.TECHNOLOGY, ApprovalStatus.APPROVED));
    }

    @Test
    void filterUsesTypeOnlyQuery() {
        List<Skill> matches = List.of(skill(5, "Scrum Master", SkillType.KNOWLEDGE, ApprovalStatus.APPROVED));
        when(skillRepository.findBySkillType(SkillType.KNOWLEDGE)).thenReturn(matches);

        assertEquals(matches, service.filterSkills(null, SkillType.KNOWLEDGE, null));
    }

    @Test
    void filterUsesStatusOnlyQuery() {
        List<Skill> matches = List.of(skill(3, "ECCP", SkillType.TECHNOLOGY, ApprovalStatus.CANCELED));
        when(skillRepository.findByStatus(ApprovalStatus.CANCELED)).thenReturn(matches);

        assertEquals(matches, service.filterSkills("", null, ApprovalStatus.CANCELED));
    }

    @Test
    void filterWithNoTypeOrStatusFallsBackToSearch() {
        List<Skill> matches = List.of(skill(1, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED));
        when(skillRepository.findBySkillNameContainingIgnoreCase("ja")).thenReturn(matches);

        assertEquals(matches, service.filterSkills("ja", null, null));
    }

    @Test
    void filterNarrowsResultsByNameWhenQueryGiven() {
        Skill dataStorage = skill(11, "Data Storage", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED);
        when(skillRepository.findBySkillType(SkillType.TECHNOLOGY)).thenReturn(List.of(
                skill(1, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED), dataStorage));

        assertEquals(List.of(dataStorage), service.filterSkills(" DATA ", SkillType.TECHNOLOGY, null));
    }

    // TC-WP-04

    @Test
    void approveMovesRequestedToApproved() {
        when(skillRepository.findById(13)).thenReturn(
                Optional.of(skill(13, "Python", SkillType.TECHNOLOGY, ApprovalStatus.REQUESTED)));
        saveReturnsItsArgument();

        assertEquals(ApprovalStatus.APPROVED, service.approveSkill(13).getStatus());
    }

    @Test
    void denyMovesRequestedToCanceled() {
        when(skillRepository.findById(13)).thenReturn(
                Optional.of(skill(13, "Python", SkillType.TECHNOLOGY, ApprovalStatus.REQUESTED)));
        saveReturnsItsArgument();

        assertEquals(ApprovalStatus.CANCELED, service.denySkill(13).getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = ApprovalStatus.class, names = {"APPROVED", "CANCELED"})
    void approveAndDenyRejectSkillsThatAreNotRequested(ApprovalStatus current) {
        when(skillRepository.findById(1)).thenReturn(
                Optional.of(skill(1, "Java", SkillType.TECHNOLOGY, current)));

        assertThrows(ResourceConflictException.class, () -> service.approveSkill(1));
        assertThrows(ResourceConflictException.class, () -> service.denySkill(1));
        verify(skillRepository, never()).save(any());
    }

    @Test
    void approveThrowsNotFoundForMissingId() {
        when(skillRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approveSkill(99));
    }

    @Test
    void deleteRemovesUnusedSkill() {
        Skill unused = skill(13, "Python", SkillType.TECHNOLOGY, ApprovalStatus.REQUESTED);
        when(skillRepository.findById(13)).thenReturn(Optional.of(unused));
        when(personSkillRepository.findBySkillId(13)).thenReturn(List.of());
        when(projectSkillRequiredRepository.findBySkillId(13)).thenReturn(List.of());

        service.deleteSkill(13);

        verify(skillRepository).delete(unused);
    }

    @Test
    void deleteRejectsSkillAssignedToPeople() {
        when(skillRepository.findById(1)).thenReturn(
                Optional.of(skill(1, "Java", SkillType.TECHNOLOGY, ApprovalStatus.APPROVED)));
        when(personSkillRepository.findBySkillId(1)).thenReturn(List.of(new PersonSkill()));
        when(projectSkillRequiredRepository.findBySkillId(1)).thenReturn(List.of());

        assertThrows(ResourceConflictException.class, () -> service.deleteSkill(1));
        verify(skillRepository, never()).delete(any());
    }

    @Test
    void deleteRejectsSkillRequiredByProjects() {
        when(skillRepository.findById(9)).thenReturn(
                Optional.of(skill(9, "Agentic AI", SkillType.OTHER, ApprovalStatus.APPROVED)));
        when(personSkillRepository.findBySkillId(9)).thenReturn(List.of());
        when(projectSkillRequiredRepository.findBySkillId(9)).thenReturn(
                List.of(new ProjectSkillRequired(4, 9, 1, RecordStatus.APPROVED)));

        assertThrows(ResourceConflictException.class, () -> service.deleteSkill(9));
        verify(skillRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsNotFoundForMissingId() {
        when(skillRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteSkill(99));
    }

    // TC-WP-05

    @Test
    void updateProficiencyLevelRenamesExistingLevelAndKeepsId() {
        SkillProficiencyLevel level = new SkillProficiencyLevel();
        level.setSkillProficiencyLevelId(2);
        level.setSkillProficiencyLevelName("Elementary");
        when(levelRepository.findById(2)).thenReturn(Optional.of(level));
        when(levelRepository.existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot("Novice", 2))
                .thenReturn(false);
        when(levelRepository.save(any(SkillProficiencyLevel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SkillProficiencyLevel renamed = service.updateProficiencyLevel(2, new ProficiencyLevelRequest(" Novice "));

        ArgumentCaptor<SkillProficiencyLevel> saved = ArgumentCaptor.forClass(SkillProficiencyLevel.class);
        verify(levelRepository).save(saved.capture());
        assertEquals(2, saved.getValue().getSkillProficiencyLevelId());
        assertEquals("Novice", renamed.getSkillProficiencyLevelName());
    }

    @Test
    void updateProficiencyLevelRejectsNameUsedByAnotherLevel() {
        SkillProficiencyLevel level = new SkillProficiencyLevel();
        level.setSkillProficiencyLevelId(2);
        when(levelRepository.findById(2)).thenReturn(Optional.of(level));
        when(levelRepository.existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot("Expert", 2))
                .thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> service.updateProficiencyLevel(2, new ProficiencyLevelRequest("Expert")));
        verify(levelRepository, never()).save(any());
    }

    @Test
    void updateProficiencyLevelThrowsNotFoundForMissingId() {
        when(levelRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProficiencyLevel(99, new ProficiencyLevelRequest("Novice")));
    }
}
