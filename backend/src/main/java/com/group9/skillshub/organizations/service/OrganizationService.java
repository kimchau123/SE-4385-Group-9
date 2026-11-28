package com.group9.skillshub.organizations.service;

import java.util.List;

import com.group9.skillshub.organizations.dto.OrganizationHierarchyDTO;
import com.group9.skillshub.organizations.dto.OrganizationPersonSkillDTO;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.dto.OrganizationSkillDistributionDTO;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.organizations.dto.OrganizationSkillCapacityDTO; 

public interface OrganizationService {
    //full organization forest, one root for each top level organization
    List<OrganizationHierarchyDTO> getOrganizationHierarchy();

    //hierarchy rooted at a single organization
    OrganizationHierarchyDTO getOrganizationSubtree(int orgID);

    //people whose memberOfOrganization equals orgID
    List<Person> getOrganizationMembers(int orgID);

    //members of orgID plus every organization beneath it in the tree
    List<Person> getOrganizationMembersIncludingSubOrganizations(int orgID);

    //Retrieve approved employee skills with names and proficiency descriptions
    List<OrganizationPersonSkillDTO> getPersonSkillDetails(int personId);

    //Retrieve top five skills
    List<OrganizationSkillDistributionDTO> getSkillDistribution(int orgId, Integer skillId, Integer proficiencyLevelId);

    //Retrieve all approved skills
    List<OrganizationSkillDistributionDTO> getSkillSummary(int orgId);

    //Filter employees by skill and proficiency
    List<Person> filterOrganizationMembers(int orgId, Integer skillId, Integer proficiencyLevelId);

    //Retrieve proficiency levels
    List<SkillProficiencyLevel> getProficiencyLevels();

    //Retrieve person ID
    Person getPersonById(int personId);

    //Retrieve the skill capacity information of a specific organization
    OrganizationSkillCapacityDTO getSkillCapacity(int orgId);

    //Retrieve the overall skill capacity accross all organizations
    OrganizationSkillCapacityDTO getOverallSkillCapacity();
}