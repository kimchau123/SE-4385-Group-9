package com.group9.skillshub.projects.service;

import com.group9.skillshub.projects.model.PersonProjectAssignment;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.ProjectSkillRequired;

import java.util.List;

public interface ProjectsService {

    Project addProject(Project project);

    Project updateProject(int id, Project project);

    void cancelProject(int id);

    List<Project> getProjects();

    List<Project> getProjectsByOrganization(int orgId);

    ProjectSkillRequired addRequiredSkill(int projectId, int skillId, int levelId);

    void cancelRequiredSkill(int projectId, int skillId);

    List<ProjectSkillRequired> getRequiredSkills(int projectId);

    List<PersonProjectAssignment> getAssignmentsByProject(int projectId);
}
