package com.group9.skillshub.organizations.dto;

import java.util.List;

//one node of the the organization tree returned to org chart view
public record OrganizationHierarchyDTO(
    int organizationID,
    String organizationName,
    Integer managerPersonId,
    String managerName,
    int memberCount, 
    List<OrganizationHierarchyDTO> children){
}
