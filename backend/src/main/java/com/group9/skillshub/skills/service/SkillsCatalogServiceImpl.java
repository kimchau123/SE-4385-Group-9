package com.group9.skillshub.skills.service;

import com.group9.skillshub.common.ResourceConflictException;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.repository.ProjectSkillRequiredRepository;
import com.group9.skillshub.skills.dto.ProficiencyLevelRequest;
import com.group9.skillshub.skills.dto.SkillRequest;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class SkillsCatalogServiceImpl implements SkillsCatalogService {

    private final SkillRepository skillRepository;
    private final SkillProficiencyLevelRepository levelRepository;
    // Used by deleteSkill to check whether a skill is in use.
    private final PersonSkillRepository personSkillRepository;
    private final ProjectSkillRequiredRepository projectSkillRequiredRepository;

    public SkillsCatalogServiceImpl(
            SkillRepository skillRepository,
            SkillProficiencyLevelRepository levelRepository,
            PersonSkillRepository personSkillRepository,
            ProjectSkillRequiredRepository projectSkillRequiredRepository) {
        this.skillRepository = skillRepository;
        this.levelRepository = levelRepository;
        this.personSkillRepository = personSkillRepository;
        this.projectSkillRequiredRepository = projectSkillRequiredRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Skill getSkillById(int id) {
        return findSkillOrThrow(id);
    }

    @Override
    @Transactional
    public Skill addSkill(SkillRequest request) {
        if (skillRepository.existsBySkillNameIgnoreCase(request.skillName())) {
            throw new ResourceConflictException(
                    "A skill named '" + request.skillName() + "' already exists");
        }
        Skill skill = new Skill();
        copyFields(request, skill);
        skill.setStatus(ApprovalStatus.REQUESTED);
        return skillRepository.save(skill);
    }

    @Override
    @Transactional
    public Skill updateSkill(int id, SkillRequest request) {
        Skill existing = findSkillOrThrow(id);
        // Skips this skill so keeping its own name isn't a conflict.
        if (skillRepository.existsBySkillNameIgnoreCaseAndSkillIdNot(request.skillName(), id)) {
            throw new ResourceConflictException(
                    "A skill named '" + request.skillName() + "' already exists");
        }
        copyFields(request, existing);
        return skillRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> searchSkills(String query) {
        if (isBlank(query)) {
            return skillRepository.findAll();
        }
        return skillRepository.findBySkillNameContainingIgnoreCase(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> filterSkills(String query, SkillType type, ApprovalStatus status) {
        List<Skill> results;
        if (type != null && status != null) {
            results = skillRepository.findBySkillTypeAndStatus(type, status);
        } else if (type != null) {
            results = skillRepository.findBySkillType(type);
        } else if (status != null) {
            results = skillRepository.findByStatus(status);
        } else {
            return searchSkills(query);
        }

        if (isBlank(query)) {
            return results;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return results.stream()
                .filter(skill -> skill.getSkillName().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    @Override
    @Transactional
    public Skill approveSkill(int id) {
        return changeStatus(id, ApprovalStatus.APPROVED);
    }

    @Override
    @Transactional
    public Skill denySkill(int id) {
        return changeStatus(id, ApprovalStatus.CANCELED);
    }

    @Override
    @Transactional
    public void deleteSkill(int id) {
        Skill skill = findSkillOrThrow(id);
        // Checked first so a skill in use gets a 409 instead of a database error (500).
        boolean usedByPeople = !personSkillRepository.findBySkillId(id).isEmpty();
        boolean usedByProjects = !projectSkillRequiredRepository.findBySkillId(id).isEmpty();
        if (usedByPeople || usedByProjects) {
            throw new ResourceConflictException("Skill " + id
                    + " is assigned to people or required by projects; deny it instead of deleting it");
        }
        skillRepository.delete(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillProficiencyLevel> getProficiencyLevels() {
        return levelRepository.findAll(Sort.by("skillProficiencyLevelId"));
    }

    @Override
    @Transactional
    public SkillProficiencyLevel updateProficiencyLevel(int id, ProficiencyLevelRequest request) {
        SkillProficiencyLevel level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proficiency level " + id + " was not found"));
        String name = request.skillProficiencyLevelName().trim();
        if (levelRepository.existsBySkillProficiencyLevelNameIgnoreCaseAndSkillProficiencyLevelIdNot(name, id)) {
            throw new ResourceConflictException("A proficiency level named '" + name + "' already exists");
        }
        level.setSkillProficiencyLevelName(name);
        return levelRepository.save(level);
    }

    private Skill findSkillOrThrow(int id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill " + id + " was not found"));
    }

    // Shared by approve and deny.
    private Skill changeStatus(int id, ApprovalStatus newStatus) {
        Skill skill = findSkillOrThrow(id);
        if (skill.getStatus() != ApprovalStatus.REQUESTED) {
            throw new ResourceConflictException("Skill " + id + " is " + skill.getStatus()
                    + "; only REQUESTED skills can be approved or denied");
        }
        skill.setStatus(newStatus);
        return skillRepository.save(skill);
    }

    private static void copyFields(SkillRequest request, Skill skill) {
        skill.setSkillName(request.skillName());
        skill.setSkillDescription(request.skillDescription());
        skill.setSkillType(request.skillType());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
