package com.group9.skillshub.projects.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectSkillRequiredId implements Serializable {

    private int projectId;
    private int skillId;
    private int skillProficiencyLevelId;

    public ProjectSkillRequiredId() {
    }

    public ProjectSkillRequiredId(int projectId, int skillId, int skillProficiencyLevelId) {
        this.projectId = projectId;
        this.skillId = skillId;
        this.skillProficiencyLevelId = skillProficiencyLevelId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getSkillProficiencyLevelId() {
        return skillProficiencyLevelId;
    }

    public void setSkillProficiencyLevelId(int skillProficiencyLevelId) {
        this.skillProficiencyLevelId = skillProficiencyLevelId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProjectSkillRequiredId that)) {
            return false;
        }
        return projectId == that.projectId
                && skillId == that.skillId
                && skillProficiencyLevelId == that.skillProficiencyLevelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, skillId, skillProficiencyLevelId);
    }
}
