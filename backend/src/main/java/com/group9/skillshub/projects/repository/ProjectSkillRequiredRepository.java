package com.group9.skillshub.projects.repository;

import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.ProjectSkillRequiredId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSkillRequiredRepository
        extends JpaRepository<ProjectSkillRequired, ProjectSkillRequiredId> {

    List<ProjectSkillRequired> findByProjectId(int projectId);

    List<ProjectSkillRequired> findBySkillId(int skillId);
}
