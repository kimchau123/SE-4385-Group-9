package com.group9.skillshub.organizations.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group9.skillshub.organizations.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
}
