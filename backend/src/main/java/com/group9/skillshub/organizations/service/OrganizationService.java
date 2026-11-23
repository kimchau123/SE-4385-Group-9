package com.group9.skillshub.organizations.service;

import java.util.List;

import com.group9.skillshub.organizations.dto.OrganizationHierarchyDTO;
import com.group9.skillshub.organizations.model.Person;

public interface OrganizationService {
    //full organization forest, one root for each top level organization
    List<OrganizationHierarchyDTO> getOrganizationHierarchy();

    //hierarchy rooted at a single organization
    OrganizationHierarchyDTO getOrganizationSubtree(int orgID);

    //people whose memberOfOrganization equals orgID
    List<Person> getOrganizationMembers(int orgID);

    //members of orgID plus every organization beneath it in the tree
    List<Person> getOrganizationMembersIncludingSubOrganizations(int orgID);
}