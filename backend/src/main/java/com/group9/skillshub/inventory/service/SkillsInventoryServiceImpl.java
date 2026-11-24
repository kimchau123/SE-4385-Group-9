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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillsInventoryServiceImpl implements SkillsInventoryService {

    private static final int MIN_LEVEL_ID = 1;
    private static final int MAX_LEVEL_ID = 5;

    private final PersonSkillRepository personSkillRepository;
    private final PersonProjectAssignmentRepository assignmentRepository;
    private final PersonRepository personRepository;
    private final SkillRepository skillRepository;
    private final SkillProficiencyLevelRepository levelRepository;
    private final ProjectRepository projectRepository;

    public SkillsInventoryServiceImpl(
            PersonSkillRepository personSkillRepository,
            PersonProjectAssignmentRepository assignmentRepository,
            PersonRepository personRepository,
            SkillRepository skillRepository,
            SkillProficiencyLevelRepository levelRepository,
            ProjectRepository projectRepository) {
        this.personSkillRepository = personSkillRepository;
        this.assignmentRepository = assignmentRepository;
        this.personRepository = personRepository;
        this.skillRepository = skillRepository;
        this.levelRepository = levelRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public PersonSkill assignSkillToPerson(int personId, int skillId, int levelId) {
        validateLevelId(levelId);
        verifyPersonExists(personId);
        verifyApprovedSkillExists(skillId);
        verifyLevelExists(levelId);
        PersonSkillId id = new PersonSkillId(personId, skillId);
        if (personSkillRepository.existsById(id)) {
            throw new ResourceConflictException(
                    "Person " + personId + " already has skill " + skillId);
        }

        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(personId);
        personSkill.setSkillId(skillId);
        personSkill.setSkillProficiencyLevelId(levelId);
        personSkill.setStatus(ApprovalStatus.REQUESTED);
        return personSkillRepository.save(personSkill);
    }

    @Override
    @Transactional
    public PersonSkill updatePersonSkill(int personId, int skillId, int levelId) {
        validateLevelId(levelId);
        verifyPersonExists(personId);
        verifyApprovedSkillExists(skillId);
        verifyLevelExists(levelId);
        PersonSkill personSkill = personSkillRepository.findById(new PersonSkillId(personId, skillId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill " + skillId + " was not found for person " + personId));
        personSkill.setSkillProficiencyLevelId(levelId);
        personSkill.setStatus(ApprovalStatus.REQUESTED);
        return personSkillRepository.save(personSkill);
    }

    @Override
    @Transactional
    public PersonSkill updatePersonSkillStatus(int personId, int skillId, ApprovalStatus status) {
        if (status != ApprovalStatus.APPROVED && status != ApprovalStatus.CANCELED) {
            throw new IllegalArgumentException("status must be APPROVED or CANCELED");
        }
        PersonSkill personSkill = personSkillRepository.findById(new PersonSkillId(personId, skillId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill " + skillId + " was not found for person " + personId));
        if (personSkill.getStatus() != ApprovalStatus.REQUESTED) {
            throw new ResourceConflictException("Only requested person skills can be reviewed");
        }
        personSkill.setStatus(status);
        return personSkillRepository.save(personSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonSkill> getSkillsByPerson(int personId) {
        verifyPersonExists(personId);
        return personSkillRepository.findByPersonId(personId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonSkill> getPersonsBySkill(int skillId) {
        verifySkillExists(skillId);
        return personSkillRepository.findBySkillId(skillId);
    }

    @Override
    @Transactional
    public PersonProjectAssignment requestAssignment(int personId, int projectId) {
        verifyPersonExists(personId);
        Project project = findProject(projectId);
        if (project.getStatus() != RecordStatus.APPROVED) {
            throw new IllegalArgumentException("Assignments can only be requested for approved projects");
        }
        PersonProjectAssignmentId id = new PersonProjectAssignmentId(personId, projectId);
        if (assignmentRepository.existsById(id)) {
            throw new ResourceConflictException(
                    "Person " + personId + " already has an assignment for project " + projectId);
        }

        PersonProjectAssignment assignment = new PersonProjectAssignment();
        assignment.setPersonId(personId);
        assignment.setProjectId(projectId);
        assignment.setStatus(ApprovalStatus.REQUESTED);
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public PersonProjectAssignment updateAssignmentStatus(
            int personId, int projectId, ApprovalStatus status) {
        if (status != ApprovalStatus.APPROVED && status != ApprovalStatus.CANCELED) {
            throw new IllegalArgumentException("status must be APPROVED or CANCELED");
        }
        PersonProjectAssignment assignment =
                assignmentRepository.findById(new PersonProjectAssignmentId(personId, projectId))
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Assignment for person " + personId + " and project " + projectId
                                        + " was not found"));
        if (assignment.getStatus() != ApprovalStatus.REQUESTED) {
            throw new ResourceConflictException("Only requested assignments can be reviewed");
        }
        assignment.setStatus(status);
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonProjectAssignment> getAssignmentsByPerson(int personId) {
        verifyPersonExists(personId);
        return assignmentRepository.findByPersonId(personId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonProjectAssignment> getAssignmentsByProject(int projectId) {
        verifyProjectExists(projectId);
        return assignmentRepository.findByProjectId(projectId);
    }

    private void verifyPersonExists(int personId) {
        if (personId <= 0) {
            throw new IllegalArgumentException("personId must be positive");
        }
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person " + personId + " was not found");
        }
    }

    private Skill verifyApprovedSkillExists(int skillId) {
        Skill skill = verifySkillExists(skillId);
        if (skill.getStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved skills can be added to an inventory");
        }
        return skill;
    }

    private Skill verifySkillExists(int skillId) {
        if (skillId <= 0) {
            throw new IllegalArgumentException("skillId must be positive");
        }
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill " + skillId + " was not found"));
    }

    private void verifyLevelExists(int levelId) {
        if (!levelRepository.existsById(levelId)) {
            throw new ResourceNotFoundException("Proficiency level " + levelId + " was not found");
        }
    }

    private void verifyProjectExists(int projectId) {
        findProject(projectId);
    }

    private Project findProject(int projectId) {
        if (projectId <= 0) {
            throw new IllegalArgumentException("projectId must be positive");
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + projectId + " was not found"));
    }

    private void validateLevelId(int levelId) {
        if (levelId < MIN_LEVEL_ID || levelId > MAX_LEVEL_ID) {
            throw new IllegalArgumentException("levelId must be between 1 and 5");
        }
    }
}
