package com.group9.skillshub.organization.repository;

import com.group9.skillshub.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
}
