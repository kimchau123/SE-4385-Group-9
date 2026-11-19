package com.group9.skillshub.projects.repository;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.PersonProjectAssignment;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.RecordStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProjectsRepositoriesTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectSkillRequiredRepository requiredSkillRepository;

    @Autowired
    private PersonProjectAssignmentRepository assignmentRepository;

    @Test
    void repositoriesPersistEntitiesAndResolveRequestedQueries() {
        Project project = new Project();
        project.setProjectName("Platform migration");
        project.setStatus(RecordStatus.APPROVED);
        project.setProjectOwningOrganizationId(4);
        project.setProjectStartDate(LocalDate.of(2026, 1, 1));
        project.setProjectEndDate(LocalDate.of(2026, 12, 31));
        project = projectRepository.save(project);

        ProjectSkillRequired requiredSkill =
                requiredSkillRepository.save(
                        new ProjectSkillRequired(project.getProjectId(), 11, 2, RecordStatus.APPROVED));
        PersonProjectAssignment assignment = new PersonProjectAssignment();
        assignment.setPersonId(8);
        assignment.setProjectId(project.getProjectId());
        assignment.setStatus(ApprovalStatus.REQUESTED);
        assignmentRepository.save(assignment);

        assertEquals(1, projectRepository.findByProjectNameContainingIgnoreCase("MIGRATION").size());
        assertEquals(1, projectRepository.findByProjectOwningOrganizationId(4).size());
        assertEquals(1, projectRepository.findByStatus(RecordStatus.APPROVED).size());
        assertEquals(requiredSkill, requiredSkillRepository.findByProjectId(project.getProjectId()).get(0));
        assertEquals(1, requiredSkillRepository.findBySkillId(11).size());
        assertEquals(1, assignmentRepository.findByProjectId(project.getProjectId()).size());
    }
}
