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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.organizations.dto.OrganizationHierarchyDTO;
import com.group9.skillshub.organizations.model.Organization;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.organizations.repository.PersonRepository;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final PersonRepository personRepository;
    
    public OrganizationServiceImpl(OrganizationRepository organizationRepository, PersonRepository personRepository){
        this.organizationRepository = organizationRepository;
        this.personRepository = personRepository;
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
        return sortByName(personRepository.findByMemberofOrganizationId(orgId));
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
        Integer parentId = manager.getMemberOfOrganizationId();
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
