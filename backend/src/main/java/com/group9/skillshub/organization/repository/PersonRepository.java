package com.group9.skillshub.organization.repository;

import com.group9.skillshub.organization.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    List<Person> findByMemberOfOrganizationId(int organizationId);
}
