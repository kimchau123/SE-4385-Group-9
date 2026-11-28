package com.group9.skillshub.projects.service;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.projects.model.PersonProjectAssignment;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.PersonProjectAssignmentRepository;
import com.group9.skillshub.projects.repository.ProjectRepository;
import com.group9.skillshub.projects.repository.ProjectSkillRequiredRepository;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectsServiceImpl implements ProjectsService {

    private final ProjectRepository projectRepository;
    private final ProjectSkillRequiredRepository projectSkillRequiredRepository;
    private final PersonProjectAssignmentRepository assignmentRepository;
    private final OrganizationRepository organizationRepository;
    private final SkillRepository skillRepository;
    private final SkillProficiencyLevelRepository levelRepository;

    public ProjectsServiceImpl(
            ProjectRepository projectRepository,
            ProjectSkillRequiredRepository projectSkillRequiredRepository,
            PersonProjectAssignmentRepository assignmentRepository,
            OrganizationRepository organizationRepository,
            SkillRepository skillRepository,
            SkillProficiencyLevelRepository levelRepository) {
        this.projectRepository = projectRepository;
        this.projectSkillRequiredRepository = projectSkillRequiredRepository;
        this.assignmentRepository = assignmentRepository;
        this.organizationRepository = organizationRepository;
        this.skillRepository = skillRepository;
        this.levelRepository = levelRepository;
    }

    @Override
    @Transactional
    public Project addProject(Project project) {
        validateProject(project);
        verifyOrganizationExists(project.getProjectOwningOrganizationId());
        project.setStatus(RecordStatus.APPROVED);
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(int id, Project project) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " was not found"));
        validateProject(project);
        verifyOrganizationExists(project.getProjectOwningOrganizationId());
        existing.setProjectName(project.getProjectName());
        existing.setProjectOwningOrganizationId(project.getProjectOwningOrganizationId());
        existing.setProjectStartDate(project.getProjectStartDate());
        existing.setProjectEndDate(project.getProjectEndDate());
        return projectRepository.save(existing);
    }

    @Override
    @Transactional
    public void cancelProject(int id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " was not found"));
        project.setStatus(RecordStatus.CANCELED);
        projectRepository.save(project);

        List<ProjectSkillRequired> requiredSkills = projectSkillRequiredRepository.findByProjectId(id);
        requiredSkills.forEach(requiredSkill -> requiredSkill.setStatus(RecordStatus.CANCELED));
        projectSkillRequiredRepository.saveAll(requiredSkills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByOrganization(int orgId) {
        return projectRepository.findByProjectOwningOrganizationId(orgId);
    }

    @Override
    @Transactional
    public ProjectSkillRequired addRequiredSkill(int projectId, int skillId, int levelId) {
        verifyProjectExists(projectId);
        if (skillId <= 0 || levelId <= 0) {
            throw new IllegalArgumentException("skillId and levelId must be positive");
        }
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill " + skillId + " was not found");
        }
        if (!levelRepository.existsById(levelId)) {
            throw new ResourceNotFoundException("Proficiency level " + levelId + " was not found");
        }
        ProjectSkillRequired requiredSkill =
                new ProjectSkillRequired(projectId, skillId, levelId, RecordStatus.APPROVED);
        return projectSkillRequiredRepository.save(requiredSkill);
    }

    @Override
    @Transactional
    public void cancelRequiredSkill(int projectId, int skillId) {
        verifyProjectExists(projectId);
        List<ProjectSkillRequired> requiredSkills =
                projectSkillRequiredRepository.findByProjectId(projectId).stream()
                        .filter(requiredSkill -> requiredSkill.getSkillId() == skillId)
                        .toList();
        if (requiredSkills.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Required skill " + skillId + " for project " + projectId + " was not found");
        }
        requiredSkills.forEach(requiredSkill -> requiredSkill.setStatus(RecordStatus.CANCELED));
        projectSkillRequiredRepository.saveAll(requiredSkills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectSkillRequired> getRequiredSkills(int projectId) {
        verifyProjectExists(projectId);
        return projectSkillRequiredRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonProjectAssignment> getAssignmentsByProject(int projectId) {
        verifyProjectExists(projectId);
        return assignmentRepository.findByProjectId(projectId);
    }

    private void verifyProjectExists(int projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project " + projectId + " was not found");
        }
    }

    private void verifyOrganizationExists(int organizationId) {
        if (organizationId <= 0) {
            throw new IllegalArgumentException("projectOwningOrganizationId must be positive");
        }
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException(
                    "Organization " + organizationId + " was not found");
        }
    }

    private void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project is required");
        }
        if (project.getProjectName() == null || project.getProjectName().isBlank()) {
            throw new IllegalArgumentException("projectName is required");
        }
        if (project.getProjectOwningOrganizationId() <= 0) {
            throw new IllegalArgumentException("projectOwningOrganizationId must be positive");
        }
        if (project.getProjectStartDate() == null || project.getProjectEndDate() == null) {
            throw new IllegalArgumentException("projectStartDate and projectEndDate are required");
        }
        if (project.getProjectEndDate().isBefore(project.getProjectStartDate())) {
            throw new IllegalArgumentException("projectEndDate must be on or after projectStartDate");
        }
    }
}
