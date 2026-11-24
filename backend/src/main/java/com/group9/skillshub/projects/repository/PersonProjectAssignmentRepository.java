package com.group9.skillshub.projects.repository;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.PersonProjectAssignment;
import com.group9.skillshub.projects.model.PersonProjectAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonProjectAssignmentRepository
        extends JpaRepository<PersonProjectAssignment, PersonProjectAssignmentId> {

    List<PersonProjectAssignment> findByProjectId(int projectId);

    List<PersonProjectAssignment> findByPersonId(int personId);

    List<PersonProjectAssignment> findByStatus(ApprovalStatus status);
}
