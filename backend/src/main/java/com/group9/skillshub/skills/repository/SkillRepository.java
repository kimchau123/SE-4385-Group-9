package com.group9.skillshub.skills.repository;

import com.group9.skillshub.skills.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Integer> {
}
