package com.group9.skillshub.projects.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "project_skill_required")
@IdClass(ProjectSkillRequiredId.class)
public class ProjectSkillRequired {

    @Id
    private int projectId;

    @Id
    private int skillId;

    @Id
    private int skillProficiencyLevelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status;

    protected ProjectSkillRequired() {
    }

    public ProjectSkillRequired(int projectId, int skillId, int levelId, RecordStatus status) {
        this.projectId = projectId;
        this.skillId = skillId;
        this.skillProficiencyLevelId = levelId;
        this.status = status;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillProficiencyLevelId() {
        return skillProficiencyLevelId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public void setSkillProficiencyLevelId(int skillProficiencyLevelId) {
        this.skillProficiencyLevelId = skillProficiencyLevelId;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }
}
