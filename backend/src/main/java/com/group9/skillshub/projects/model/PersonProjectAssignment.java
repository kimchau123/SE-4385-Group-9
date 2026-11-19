package com.group9.skillshub.projects.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "person_project_assignment")
@IdClass(PersonProjectAssignmentId.class)
public class PersonProjectAssignment {

    @Id
    private int personId;

    @Id
    private int projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    public PersonProjectAssignment() {
    }

    public int getPersonId() {
        return personId;
    }

    public int getProjectId() {
        return projectId;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
