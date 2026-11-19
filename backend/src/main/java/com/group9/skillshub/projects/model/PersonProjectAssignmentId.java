package com.group9.skillshub.projects.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PersonProjectAssignmentId implements Serializable {

    private int personId;
    private int projectId;

    public PersonProjectAssignmentId() {
    }

    public PersonProjectAssignmentId(int personId, int projectId) {
        this.personId = personId;
        this.projectId = projectId;
    }

    public int getPersonId() {
        return personId;
    }

    public int getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonProjectAssignmentId that)) {
            return false;
        }
        return personId == that.personId && projectId == that.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, projectId);
    }
}
