package com.group9.skillshub.inventory.repository;

import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.model.PersonSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonSkillRepository extends JpaRepository<PersonSkill, PersonSkillId> {

    List<PersonSkill> findByPersonId(int personId);

    List<PersonSkill> findBySkillId(int skillId);

    List<PersonSkill> findBySkillIdAndSkillProficiencyLevelId(int skillId, int levelId);
}
