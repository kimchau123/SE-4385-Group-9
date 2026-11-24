package com.group9.skillshub.inventory.service;

import com.group9.skillshub.common.ResourceConflictException;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.model.PersonSkillId;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.organization.repository.PersonRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.PersonProjectAssignment;
import com.group9.skillshub.projects.model.PersonProjectAssignmentId;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.PersonProjectAssignmentRepository;
import com.group9.skillshub.projects.repository.ProjectRepository;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillsInventoryServiceImplTest {

    @Mock
    private PersonSkillRepository personSkillRepository;
    @Mock
    private PersonProjectAssignmentRepository assignmentRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillProficiencyLevelRepository levelRepository;
    @Mock
    private ProjectRepository projectRepository;

    private SkillsInventoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SkillsInventoryServiceImpl(
                personSkillRepository, assignmentRepository, personRepository,
                skillRepository, levelRepository, projectRepository);
    }

    @Test
    void assignSkillToPersonCreatesRequestedSkillAfterValidatingReferences() {
        when(personRepository.existsById(4)).thenReturn(true);
        when(skillRepository.findById(8)).thenReturn(Optional.of(approvedSkill()));
        when(levelRepository.existsById(3)).thenReturn(true);
        when(personSkillRepository.existsById(new PersonSkillId(4, 8))).thenReturn(false);

        service.assignSkillToPerson(4, 8, 3);

        ArgumentCaptor<PersonSkill> captor = ArgumentCaptor.forClass(PersonSkill.class);
        verify(personSkillRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getPersonId());
        assertEquals(8, captor.getValue().getSkillId());
        assertEquals(3, captor.getValue().getSkillProficiencyLevelId());
        assertEquals(ApprovalStatus.REQUESTED, captor.getValue().getStatus());
    }

    @Test
    void assignSkillToPersonRejectsInvalidLevelAndDuplicateMapping() {
        assertThrows(IllegalArgumentException.class, () -> service.assignSkillToPerson(4, 8, 6));

        when(personRepository.existsById(4)).thenReturn(true);
        when(skillRepository.findById(8)).thenReturn(Optional.of(approvedSkill()));
        when(levelRepository.existsById(3)).thenReturn(true);
        when(personSkillRepository.existsById(new PersonSkillId(4, 8))).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> service.assignSkillToPerson(4, 8, 3));
    }

    @Test
    void updatePersonSkillSetsRequestedStatusAndNewLevel() {
        PersonSkill existing = personSkill(4, 8, 1, ApprovalStatus.APPROVED);
        when(personRepository.existsById(4)).thenReturn(true);
        when(skillRepository.findById(8)).thenReturn(Optional.of(approvedSkill()));
        when(levelRepository.existsById(4)).thenReturn(true);
        when(personSkillRepository.findById(new PersonSkillId(4, 8))).thenReturn(Optional.of(existing));

        service.updatePersonSkill(4, 8, 4);

        assertEquals(4, existing.getSkillProficiencyLevelId());
        assertEquals(ApprovalStatus.REQUESTED, existing.getStatus());
        verify(personSkillRepository).save(existing);
    }

    @Test
    void updatePersonSkillReturnsNotFoundWhenAssociationIsMissing() {
        when(personRepository.existsById(4)).thenReturn(true);
        when(skillRepository.findById(8)).thenReturn(Optional.of(approvedSkill()));
        when(levelRepository.existsById(4)).thenReturn(true);
        when(personSkillRepository.findById(new PersonSkillId(4, 8))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updatePersonSkill(4, 8, 4));
    }

    @Test
    void updatePersonSkillStatusApprovesOnlyPendingRequests() {
        PersonSkill pending = personSkill(4, 8, 2, ApprovalStatus.REQUESTED);
        PersonSkill rejected = personSkill(4, 9, 3, ApprovalStatus.REQUESTED);
        when(personSkillRepository.findById(new PersonSkillId(4, 8))).thenReturn(Optional.of(pending));
        when(personSkillRepository.findById(new PersonSkillId(4, 9))).thenReturn(Optional.of(rejected));

        service.updatePersonSkillStatus(4, 8, ApprovalStatus.APPROVED);
        service.updatePersonSkillStatus(4, 9, ApprovalStatus.CANCELED);

        assertEquals(ApprovalStatus.APPROVED, pending.getStatus());
        assertEquals(ApprovalStatus.CANCELED, rejected.getStatus());
        verify(personSkillRepository).save(pending);
        verify(personSkillRepository).save(rejected);
        assertThrows(IllegalArgumentException.class,
                () -> service.updatePersonSkillStatus(4, 8, ApprovalStatus.REQUESTED));
    }

    @Test
    void requestAssignmentCreatesRequestedAssignmentForExistingPersonAndProject() {
        when(personRepository.existsById(4)).thenReturn(true);
        when(projectRepository.findById(7)).thenReturn(Optional.of(approvedProject()));
        when(assignmentRepository.existsById(new PersonProjectAssignmentId(4, 7))).thenReturn(false);

        service.requestAssignment(4, 7);

        ArgumentCaptor<PersonProjectAssignment> captor =
                ArgumentCaptor.forClass(PersonProjectAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getPersonId());
        assertEquals(7, captor.getValue().getProjectId());
        assertEquals(ApprovalStatus.REQUESTED, captor.getValue().getStatus());
    }

    @Test
    void requestAssignmentReturnsNotFoundForMissingProject() {
        when(personRepository.existsById(4)).thenReturn(true);
        when(projectRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requestAssignment(4, 7));
    }

    @Test
    void updateAssignmentStatusTransitionsPendingAssignment() {
        PersonProjectAssignment pending = new PersonProjectAssignment();
        pending.setPersonId(4);
        pending.setProjectId(7);
        pending.setStatus(ApprovalStatus.REQUESTED);
        when(assignmentRepository.findById(new PersonProjectAssignmentId(4, 7)))
                .thenReturn(Optional.of(pending));

        service.updateAssignmentStatus(4, 7, ApprovalStatus.CANCELED);

        assertEquals(ApprovalStatus.CANCELED, pending.getStatus());
        verify(assignmentRepository).save(pending);
    }

    private Skill approvedSkill() {
        Skill skill = new Skill();
        skill.setSkillId(8);
        skill.setStatus(ApprovalStatus.APPROVED);
        return skill;
    }

    private Project approvedProject() {
        Project project = new Project();
        project.setProjectId(7);
        project.setStatus(RecordStatus.APPROVED);
        return project;
    }

    private PersonSkill personSkill(int personId, int skillId, int levelId, ApprovalStatus status) {
        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(personId);
        personSkill.setSkillId(skillId);
        personSkill.setSkillProficiencyLevelId(levelId);
        personSkill.setStatus(status);
        return personSkill;
    }
}
