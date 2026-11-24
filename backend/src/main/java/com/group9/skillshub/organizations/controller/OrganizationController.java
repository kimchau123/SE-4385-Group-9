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
        return ResponseEntity.ok(includeSubOrganizations ? organizationService.getOrganizationMembersIncludingSubOrganizations(orgID)
        : organizationService.getOrganizationMembers(orgID));
    }

}