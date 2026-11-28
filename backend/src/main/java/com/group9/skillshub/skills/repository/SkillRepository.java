package com.group9.skillshub.skills.repository;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Integer> {

    boolean existsBySkillNameIgnoreCase(String skillName);

    // Duplicate check on update
    boolean existsBySkillNameIgnoreCaseAndSkillIdNot(String skillName, int skillId);

    // Search: full or partial name, case-insensitive
    List<Skill> findBySkillNameContainingIgnoreCase(String query);

    List<Skill> findBySkillType(SkillType skillType);

    List<Skill> findByStatus(ApprovalStatus status);

    List<Skill> findBySkillTypeAndStatus(SkillType skillType, ApprovalStatus status);
}
