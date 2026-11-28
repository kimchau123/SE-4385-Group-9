package com.group9.skillshub.skills.service;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.dto.ProficiencyLevelRequest;
import com.group9.skillshub.skills.dto.SkillRequest;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;

import java.util.List;

// Business rules for the Skills Catalog; missing records throw 404 and conflicts throw 409.
public interface SkillsCatalogService {

    List<Skill> getAllSkills();

    Skill getSkillById(int id);

    // New skills always start as REQUESTED.
    Skill addSkill(SkillRequest request);

    // Keeps the skill's current status.
    Skill updateSkill(int id, SkillRequest request);

    // A blank query returns every skill.
    List<Skill> searchSkills(String query);

    // A null filter is ignored.
    List<Skill> filterSkills(String query, SkillType type, ApprovalStatus status);

    // Only REQUESTED skills can be approved or denied.
    Skill approveSkill(int id);

    Skill denySkill(int id);

    // Fails with 409 if a person or project still uses the skill.
    void deleteSkill(int id);

    List<SkillProficiencyLevel> getProficiencyLevels();

    // Levels can be renamed but not added. The scale is fixed at 1-5.
    SkillProficiencyLevel updateProficiencyLevel(int id, ProficiencyLevelRequest request);
}
