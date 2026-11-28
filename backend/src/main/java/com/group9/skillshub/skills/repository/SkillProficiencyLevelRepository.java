package com.group9.skillshub.skills.repository;

import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillProficiencyLevelRepository extends JpaRepository<SkillProficiencyLevel, Integer> {

    Optional<SkillProficiencyLevel> findBySkillProficiencyLevelName(String name);

    // Duplicate check when renaming a level
    boolean existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot(String name, int id);
}
