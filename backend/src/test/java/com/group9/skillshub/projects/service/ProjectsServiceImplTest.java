package com.group9.skillshub.projects.service;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.PersonProjectAssignmentRepository;
import com.group9.skillshub.projects.repository.ProjectRepository;
import com.group9.skillshub.projects.repository.ProjectSkillRequiredRepository;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectSkillRequiredRepository requiredSkillRepository;

    @Mock
    private PersonProjectAssignmentRepository assignmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillProficiencyLevelRepository levelRepository;

    private ProjectsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectsServiceImpl(
                projectRepository, requiredSkillRepository, assignmentRepository,
                organizationRepository, skillRepository, levelRepository);
    }

    @Test
    void addProjectApprovesAndPersistsValidProject() {
        Project project = validProject();
        when(organizationRepository.existsById(2)).thenReturn(true);
        when(projectRepository.save(project)).thenReturn(project);

        Project saved = service.addProject(project);

        assertEquals(RecordStatus.APPROVED, saved.getStatus());
        verify(projectRepository).save(project);
    }

    @Test
    void addProjectRejectsEndDateBeforeStartDate() {
        Project project = validProject();
        project.setProjectEndDate(project.getProjectStartDate().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.addProject(project));
    }

    @Test
    void addRequiredSkillRequiresExistingProjectAndApprovesRecord() {
        when(projectRepository.existsById(7)).thenReturn(true);
        when(skillRepository.existsById(12)).thenReturn(true);
        when(levelRepository.existsById(3)).thenReturn(true);
        ArgumentCaptor<ProjectSkillRequired> captor = ArgumentCaptor.forClass(ProjectSkillRequired.class);

        service.addRequiredSkill(7, 12, 3);

        verify(requiredSkillRepository).save(captor.capture());
        ProjectSkillRequired saved = captor.getValue();
        assertEquals(7, saved.getProjectId());
        assertEquals(12, saved.getSkillId());
        assertEquals(3, saved.getSkillProficiencyLevelId());
        assertEquals(RecordStatus.APPROVED, saved.getStatus());
    }

    @Test
    void addRequiredSkillReturnsNotFoundWhenProjectDoesNotExist() {
        when(projectRepository.existsById(404)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.addRequiredSkill(404, 12, 3));
    }

    @Test
    void addRequiredSkillRejectsMissingSkillAndLevel() {
        when(projectRepository.existsById(7)).thenReturn(true);
        when(skillRepository.existsById(404)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.addRequiredSkill(7, 404, 3));

        when(skillRepository.existsById(12)).thenReturn(true);
        when(levelRepository.existsById(404)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.addRequiredSkill(7, 12, 404));
    }

    @Test
    void addProjectRejectsMissingOrganization() {
        Project project = validProject();
        when(organizationRepository.existsById(2)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.addProject(project));
    }

    @Test
    void cancelProjectCancelsProjectAndAllRequiredSkills() {
        Project project = validProject();
        project.setStatus(RecordStatus.APPROVED);
        ProjectSkillRequired first = new ProjectSkillRequired(7, 12, 2, RecordStatus.APPROVED);
        ProjectSkillRequired second = new ProjectSkillRequired(7, 15, 3, RecordStatus.APPROVED);
        when(projectRepository.findById(7)).thenReturn(Optional.of(project));
        when(requiredSkillRepository.findByProjectId(7)).thenReturn(List.of(first, second));

        service.cancelProject(7);

        assertEquals(RecordStatus.CANCELED, project.getStatus());
        assertEquals(RecordStatus.CANCELED, first.getStatus());
        assertEquals(RecordStatus.CANCELED, second.getStatus());
        verify(projectRepository).save(project);
        verify(requiredSkillRepository).saveAll(List.of(first, second));
    }

    @Test
    void cancelProjectReturnsNotFoundWhenProjectDoesNotExist() {
        when(projectRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.cancelProject(404));
    }

    private Project validProject() {
        Project project = new Project();
        project.setProjectName("Skills Hub rollout");
        project.setProjectOwningOrganizationId(2);
        project.setProjectStartDate(LocalDate.of(2026, 1, 1));
        project.setProjectEndDate(LocalDate.of(2026, 12, 31));
        return project;
    }
}
