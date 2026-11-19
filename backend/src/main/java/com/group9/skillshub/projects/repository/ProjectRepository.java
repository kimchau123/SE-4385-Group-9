package com.group9.skillshub.projects.repository;

import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    List<Project> findByProjectNameContainingIgnoreCase(String name);

    List<Project> findByProjectOwningOrganizationId(int orgId);

    List<Project> findByStatus(RecordStatus status);
}
