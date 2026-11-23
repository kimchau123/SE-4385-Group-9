package com.group9.skillshub.skills.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/** Seeded with ids 1-5 (Beginner = 1 ... Expert = 5) by ProficiencyLevelSeeder. */
@Entity
@Table(name = "skill_proficiency_level")
public class SkillProficiencyLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int skillProficiencyLevelId;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String skillProficiencyLevelName;

    public int getSkillProficiencyLevelId() {
        return skillProficiencyLevelId;
    }

    public void setSkillProficiencyLevelId(int skillProficiencyLevelId) {
        this.skillProficiencyLevelId = skillProficiencyLevelId;
    }

    public String getSkillProficiencyLevelName() {
        return skillProficiencyLevelName;
    }

    public void setSkillProficiencyLevelName(String skillProficiencyLevelName) {
        this.skillProficiencyLevelName = skillProficiencyLevelName;
    }
}
