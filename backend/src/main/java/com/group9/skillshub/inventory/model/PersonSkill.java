package com.group9.skillshub.inventory.model;

import com.group9.skillshub.projects.model.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** Skills Inventory: one person's rating for one skill. New and updated ratings are REQUESTED. */
@Entity
@Table(name = "person_skill")
@IdClass(PersonSkillId.class)
public class PersonSkill {

    @Id
    private int personId;

    @Id
    private int skillId;

    @Column(nullable = false)
    private int skillProficiencyLevelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.REQUESTED;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
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

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
