package com.group9.skillshub.inventory.service;

import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.PersonProjectAssignment;

import java.util.List;

public interface SkillsInventoryService {

    PersonSkill assignSkillToPerson(int personId, int skillId, int levelId);

    PersonSkill updatePersonSkill(int personId, int skillId, int levelId);

    PersonSkill updatePersonSkillStatus(int personId, int skillId, ApprovalStatus status);

    List<PersonSkill> getSkillsByPerson(int personId);

    List<PersonSkill> getPersonsBySkill(int skillId);

    PersonProjectAssignment requestAssignment(int personId, int projectId);

    PersonProjectAssignment updateAssignmentStatus(int personId, int projectId, ApprovalStatus status);

    List<PersonProjectAssignment> getAssignmentsByPerson(int personId);

    List<PersonProjectAssignment> getAssignmentsByProject(int projectId);
}
