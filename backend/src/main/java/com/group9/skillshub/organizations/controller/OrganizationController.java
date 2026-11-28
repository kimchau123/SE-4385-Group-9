package com.group9.skillshub.organizations.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group9.skillshub.organizations.service.OrganizationService;

@RestController 
@RequestMapping("/api/organizations")
public class OrganizationController{

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService){
        this.organizationService = organizationService;
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<?> getHierarchy() {
        return ResponseEntity.ok(organizationService.getOrganizationHierarchy());
    }
    
    @GetMapping("/{orgID}/hierarchy")
    public ResponseEntity<?> getSubtree(@PathVariable int orgID){
        return ResponseEntity.ok(organizationService.getOrganizationSubtree(orgID));
    }

    @GetMapping("/{orgID}/members")
    public ResponseEntity<?> getOrganizationMembers(@PathVariable int orgID, @RequestParam(defaultValue="false") boolean includeSubOrganizations){
        return ResponseEntity.ok(includeSubOrganizations ? organizationService.getOrganizationMembersIncludingSubOrganizations(orgID): organizationService.getOrganizationMembers(orgID));
    }

    @GetMapping("/persons/{personId}/skills")
    public ResponseEntity<?> getPersonSkillDetails(@PathVariable int personId) {
        return ResponseEntity.ok(organizationService.getPersonSkillDetails(personId));
    }

    @GetMapping("/{orgId}/skills-distribution") 
    public ResponseEntity<?> getSkillDistribution(@PathVariable int orgId, @RequestParam(required = false) Integer skillId, @RequestParam(required = false) Integer proficiencyLevelId ) { 
        return ResponseEntity.ok( organizationService.getSkillDistribution( orgId, skillId, proficiencyLevelId )); 
    }

    @GetMapping("/{orgId}/skills-summary") 
    public ResponseEntity<?> getSkillSummary(@PathVariable int orgId) { 
        return ResponseEntity.ok(organizationService.getSkillSummary(orgId)); 
    }

    @GetMapping("/{orgId}/members/filter") 
    public ResponseEntity<?> filterOrganizationMembers( @PathVariable int orgId, @RequestParam(required = false) Integer skillId, @RequestParam(required = false) Integer proficiencyLevelId ) { 
        return ResponseEntity.ok(organizationService.filterOrganizationMembers( orgId, skillId, proficiencyLevelId )); 
    }

    @GetMapping("/proficiency-levels")
    public ResponseEntity<?> getProficiencyLevels() {
        return ResponseEntity.ok(organizationService.getProficiencyLevels());
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<?> getPersonById(@PathVariable int personId) {
        return ResponseEntity.ok(organizationService.getPersonById(personId));
    }

    @GetMapping("/{orgId}/skills-capacity") 
    public ResponseEntity<?> getSkillCapacity(@PathVariable int orgId) { 
        return ResponseEntity.ok(organizationService.getSkillCapacity(orgId)); 
    }

    @GetMapping("/skills-capacity") 
    public ResponseEntity<?> getOverallSkillCapacity() { 
        return ResponseEntity.ok(organizationService.getOverallSkillCapacity()); 
    }
}