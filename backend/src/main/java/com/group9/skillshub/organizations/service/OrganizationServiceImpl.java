package com.group9.skillshub.organizations.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.organizations.dto.OrganizationHierarchyDTO;
import com.group9.skillshub.organizations.model.Organization;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.organizations.repository.PersonRepository;
import com.group9.skillshub.organizations.dto.OrganizationPersonSkillDTO;
import com.group9.skillshub.organizations.dto.OrganizationSkillDistributionDTO;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.repository.PersonSkillRepository;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.repository.SkillRepository;
import com.group9.skillshub.skills.repository.SkillProficiencyLevelRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.organizations.dto.OrganizationSkillCapacityDTO;
import java.util.LinkedHashMap;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final PersonRepository personRepository;
    private final PersonSkillRepository personSkillRepository;
    private final SkillRepository skillRepository;
    private final SkillProficiencyLevelRepository skillProficiencyLevelRepository;

    public OrganizationServiceImpl(
        OrganizationRepository organizationRepository, 
        PersonRepository personRepository, 
        PersonSkillRepository personSkillRepository,
        SkillRepository skillRepository, 
        SkillProficiencyLevelRepository skillProficiencyLevelRepository) {
        this.organizationRepository = organizationRepository;
        this.personRepository = personRepository;
        this.personSkillRepository = personSkillRepository;
        this.skillRepository = skillRepository;
        this.skillProficiencyLevelRepository = skillProficiencyLevelRepository;
    }
        private static final class Snapshot {
        final Map<Integer, Organization> orgsById = new LinkedHashMap<>();
        final Map<Integer, Person> personsById = new HashMap<>();
        final Map<Integer, Integer> memberCounts = new HashMap<>();
        final Map<Integer, Integer> parentOf = new HashMap<>();
        final Map<Integer, List<Integer>> childrenOf = new HashMap<>();
    }

    @Override 
    @Transactional (readOnly = true)
    public List<OrganizationHierarchyDTO> getOrganizationHierarchy(){
        Snapshot snapshot = loadSnapshot();
        Set<Integer> visited = new HashSet<>();
        List<OrganizationHierarchyDTO> roots = new ArrayList<>();

        for(Integer orgId : snapshot.orgsById.keySet()){
            if(snapshot.parentOf.get(orgId) == null){
                roots.add(buildNode(orgId, snapshot, visited));
            }
        }
        for(Integer orgId : snapshot.orgsById.keySet()){
            if(!visited.contains(orgId)){
                roots.add(buildNode(orgId, snapshot, visited));
            }
        }
        return roots;
    }

    @Override 
    @Transactional(readOnly=true)
    public OrganizationHierarchyDTO getOrganizationSubtree(int orgId){
        Snapshot snapshot = loadSnapshot();
        verifyOrganizationExists(orgId, snapshot);
        return buildNode(orgId, snapshot, new HashSet<>());
    }

    @Override 
    @Transactional(readOnly=true)
    public List<Person> getOrganizationMembers(int orgId){
        if(!organizationRepository.existsById(orgId)){
            throw new ResourceNotFoundException("Organization "+ orgId + " was not found");
        }
        return sortByName(personRepository.findByMemberOfOrganizationId(orgId));
    }

    @Override 
    @Transactional(readOnly=true)
    public List<Person> getOrganizationMembersIncludingSubOrganizations(int orgId){
        Snapshot snapshot = loadSnapshot();
        verifyOrganizationExists(orgId, snapshot);

        Set<Integer> orgIds = new LinkedHashSet<>();
        Deque<Integer> pending = new ArrayDeque<>();
        pending.push(orgId);
        while(!pending.isEmpty()){
            Integer current = pending.pop();
            if(orgIds.add(current)){
                snapshot.childrenOf.getOrDefault(current, List.of()).forEach(pending::push);
            }
        }
        return sortByName(personRepository.findByMemberOfOrganizationIdIn(orgIds));
    }

    @Override 
    @Transactional(readOnly = true)
    public List <OrganizationPersonSkillDTO> getPersonSkillDetails(int personId) {
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException(
                "Person " + personId + " was not found"
            );
        }

        List<PersonSkill> personSkills = 
            personSkillRepository.findByPersonId(personId);

        List<OrganizationPersonSkillDTO> result = 
            new ArrayList<>();
    
        for (PersonSkill personSkill: personSkills) {
            if (personSkill.getStatus() != ApprovalStatus.APPROVED) {
                continue;
            }

            Skill skill = skillRepository   
                .findById(personSkill.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Skill " + personSkill.getSkillId() + " was not found"
                ));
            
            SkillProficiencyLevel level = 
                skillProficiencyLevelRepository
                    .findById(personSkill.getSkillProficiencyLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Proficiency level was not found"
                    ));
            
            result.add(new OrganizationPersonSkillDTO(
                personSkill.getPersonId(),
                skill.getSkillId(),
                skill.getSkillName(),
                level.getSkillProficiencyLevelId(),
                level.getSkillProficiencyLevelName(),
                personSkill.getStatus().name()
            ));
        }

        return result;
    }

    @Override 
    @Transactional(readOnly = true)
    public List<OrganizationSkillDistributionDTO> getSkillSummary(int orgId){
        // Retrieve employees from the organization and its suborganizations
        List<Person> members = getOrganizationMembers(orgId);
        //Extract employee IDs
        List<Integer> personIds = members.stream()
            .map(Person::getPersonId)
            .toList();
        
        if (personIds.isEmpty()) {
            return List.of();
        }
        
        // Retrieve all approved employee skills
        List<PersonSkill> approvedSkills = 
            personSkillRepository.findByPersonIdInAndStatus(
               personIds, ApprovalStatus.APPROVED
        );

        // Group employees by skill ID
        Map<Integer, Set<Integer>> employeesBySkill = new HashMap<>();

        for (PersonSkill personSkill : approvedSkills) {
            employeesBySkill.computeIfAbsent(personSkill.getSkillId(), id -> new HashSet<>()).add(personSkill.getPersonId());
        }

        // Retrieve skills names and create DTOs
        List<OrganizationSkillDistributionDTO> result = new ArrayList<>();

        for (Skill skill : skillRepository.findAllById(employeesBySkill.keySet())) {
            result.add(new OrganizationSkillDistributionDTO(
                skill.getSkillId(),
                skill.getSkillName(),
                employeesBySkill.get(skill.getSkillId()).size()
            ));
        }

        //Sort all approved sills by employee count
        return result.stream()
            .sorted(
                Comparator.comparingLong(OrganizationSkillDistributionDTO::employeeCount)
                .reversed()
                .thenComparing(OrganizationSkillDistributionDTO::skillName)
                .thenComparing(OrganizationSkillDistributionDTO::skillId))
            .toList();
    }

    @Override 
    @Transactional(readOnly = true) 
    public List<OrganizationSkillDistributionDTO> getSkillDistribution( int orgId, Integer skillId, Integer proficiencyLevelId ) { 
        List<Person> members = getOrganizationMembers(orgId); 
        List<Integer> personIds = members.stream() .map(Person::getPersonId) .toList(); 
        if (personIds.isEmpty()) { 
            return List.of(); 
        } 
        List<PersonSkill> approvedSkills = personSkillRepository.findByPersonIdInAndStatus( personIds, ApprovalStatus.APPROVED ); 
        Map<Integer, Set<Integer>> employeesBySkill = new HashMap<>(); 
        for (PersonSkill skill : approvedSkills) { 
            boolean matchesSkill = skillId == null || skill.getSkillId() == skillId; 
            boolean matchesLevel = proficiencyLevelId == null || skill.getSkillProficiencyLevelId() == proficiencyLevelId; 
            if (matchesSkill && matchesLevel) { 
                employeesBySkill.computeIfAbsent(skill.getSkillId(), id -> new HashSet<>()).add(skill.getPersonId());
            } 
        } 
        List<OrganizationSkillDistributionDTO> result = new ArrayList<>(); 
        for (Skill skill : skillRepository.findAllById(employeesBySkill.keySet())) {
            result.add(new OrganizationSkillDistributionDTO(skill.getSkillId(), skill.getSkillName(), employeesBySkill.get(skill.getSkillId()).size()));
        } 
        return result.stream().sorted(Comparator.comparingLong( OrganizationSkillDistributionDTO::employeeCount ).reversed().thenComparing( OrganizationSkillDistributionDTO::skillName )).limit(5).toList(); 
    }

    @Override 
    @Transactional(readOnly = true) 
    public List<Person> filterOrganizationMembers( int orgId, Integer skillId, Integer proficiencyLevelId ) { 
        // Retrieve employees from the selected organization 
        List<Person> members = getOrganizationMembers(orgId);
        //Return all employees if no filters are selected 
        if (skillId == null && proficiencyLevelId == null) { 
            return members; 
        } 
        // Extract employee IDs 
        List<Integer> personIds = members.stream() 
            .map(Person::getPersonId) 
            .toList(); 
        if (personIds.isEmpty()) { 
            return List.of(); 
        } 
        // Retrieve approved employee skills 
        List<PersonSkill> approvedSkills = 
            personSkillRepository.findByPersonIdInAndStatus(personIds, ApprovalStatus.APPROVED); 
        // Find employees matching the selected filters 
        Set<Integer> matchingPersonIds = new HashSet<>(); 
        for (PersonSkill personSkill : approvedSkills) { 
            boolean matchesSkill = skillId == null || personSkill.getSkillId() == skillId; 
            boolean matchesProficiency = proficiencyLevelId == null || personSkill.getSkillProficiencyLevelId() == proficiencyLevelId; 
        if (matchesSkill && matchesProficiency) { 
            matchingPersonIds.add(personSkill.getPersonId()); 
        } 
    } 
    // Return only matching employees 
    return members.stream() 
        .filter(person -> 
            matchingPersonIds.contains(person.getPersonId()) 
        ) 
        .toList(); 
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillProficiencyLevel> getProficiencyLevels() {
        return skillProficiencyLevelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Person getPersonById(int personId) {
        return personRepository.findById(personId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Person " + personId + " was not found"
            ));
    }
    
    @Override 
    @Transactional(readOnly = true) 
    public OrganizationSkillCapacityDTO getSkillCapacity(int orgId) { 
        return calculateSkillCapacity(getOrganizationMembers(orgId)); 
    } 

    @Override 
    @Transactional(readOnly = true) 
    public OrganizationSkillCapacityDTO getOverallSkillCapacity() { 
        return calculateSkillCapacity(personRepository.findAll()); 
    } 
    private OrganizationSkillCapacityDTO calculateSkillCapacity( List<Person> members ) { 
        int totalEmployees = members.size(); 
        List<Integer> personIds = members.stream()
            .map(Person::getPersonId)
            .toList();
        List<PersonSkill> approvedSkills = personIds.isEmpty()
            ? List.of()
            : personSkillRepository.findByPersonIdInAndStatus(
                personIds, ApprovalStatus.APPROVED
            );
        Set<Integer> employeesWithSkills = new HashSet<>();
        Set<Integer> distinctSkills = new HashSet<>();
        Map<Integer, String> levelNames = new HashMap<>();
        Map<String, Long> proficiencyDistribution = new LinkedHashMap<>();
        for (SkillProficiencyLevel level :
                skillProficiencyLevelRepository.findAll()) {
            levelNames.put(
                level.getSkillProficiencyLevelId(),
                level.getSkillProficiencyLevelName()
            );
            proficiencyDistribution.put(
                level.getSkillProficiencyLevelName(), 0L
            );
        }
        for (PersonSkill skill : approvedSkills) {
            employeesWithSkills.add(skill.getPersonId());
            distinctSkills.add(skill.getSkillId());
            String levelName = levelNames.getOrDefault(
                skill.getSkillProficiencyLevelId(),
                "Unknown"
            );
            proficiencyDistribution.merge(levelName, 1L, Long::sum);
        }
        int withSkills = employeesWithSkills.size();
        int withoutSkills = totalEmployees - withSkills;
        double coverage = 0.0;
        if (totalEmployees > 0) {
            coverage = (withSkills * 100.0) / totalEmployees;
        }
        return new OrganizationSkillCapacityDTO(
            totalEmployees,
            withSkills,
            withoutSkills,
            distinctSkills.size(),
            coverage,
            proficiencyDistribution
        );
    }
    private OrganizationHierarchyDTO buildNode(int orgId, Snapshot snapshot, Set<Integer> visited){
        visited.add(orgId);
        Organization org = snapshot.orgsById.get(orgId);

        List<OrganizationHierarchyDTO> children = new ArrayList<>();
        for(Integer childId : snapshot.childrenOf.getOrDefault(orgId,List.of())){
            if(!visited.contains(childId)){
                children.add(buildNode(childId, snapshot, visited));
            }
        }

        Integer managerId = org.getOrganizationManagerPersonId();
        Person manager = managerId == null ? null : snapshot.personsById.get(managerId);
        return new OrganizationHierarchyDTO(
                org.getOrganizationId(),
                org.getOrganizationName(),
                managerId,
                manager == null ? null : manager.getPersonName(),
                snapshot.memberCounts.getOrDefault(orgId, 0),
                children);
    }

    private Snapshot loadSnapshot(){
        List<Organization> orgs = new ArrayList<>(organizationRepository.findAll());
        orgs.sort(Comparator.comparing(Organization::getOrganizationName, String.CASE_INSENSITIVE_ORDER));
        List<Person> people = personRepository.findAll();

        Snapshot s = new Snapshot();

        orgs.forEach(o -> s.orgsById.put(o.getOrganizationId(), o));

        for(Person p : people){
            s.personsById.put(p.getPersonId(), p);
            if(p.getMemberOfOrganizationId() != null){
                s.memberCounts.merge(p.getMemberOfOrganizationId(), 1, Integer::sum);
            }
        }
         for (Organization o : orgs) {
            Integer parentId = resolveParentId(o, s);
            s.parentOf.put(o.getOrganizationId(), parentId);
            if (parentId != null) {
                s.childrenOf.computeIfAbsent(parentId, k -> new ArrayList<>()).add(o.getOrganizationId());
            }
        }
        return s;
    }

    private Integer resolveParentId(Organization org, Snapshot s) {
        Integer managerId = org.getOrganizationManagerPersonId();
        if (managerId == null) {
            return null;
        }
        Person manager = s.personsById.get(managerId);
        if (manager == null) {
            return null;
        }
        // Find the person the department manager reports to
        Integer supervisorId = manager.getManagerPersonId();
        if (supervisorId == null) {
            return null;
        }
        Person supervisor = s.personsById.get(supervisorId);
        if (supervisor == null) {
            return null;
        }
        // Find the supervisor's organization
        Integer parentId = supervisor.getMemberOfOrganizationId();
        if (parentId == null
                || parentId == org.getOrganizationId()
                || !s.orgsById.containsKey(parentId)) {
            return null;
        }
        return parentId;
    }

    private void verifyOrganizationExists(int orgId, Snapshot snapshot) {
        if (!snapshot.orgsById.containsKey(orgId)) {
            throw new ResourceNotFoundException("Organization " + orgId + " was not found");
        }
    }

     private List<Person> sortByName(List<Person> people) {
        List<Person> sorted = new ArrayList<>(people);
        sorted.sort(Comparator.comparing(Person::getPersonName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }
}
