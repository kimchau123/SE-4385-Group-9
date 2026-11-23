package com.group9.skillshub.organizations.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group9.skillshub.organizations.model.Person;

public interface PersonRepository extends JpaRepository<Person, Integer> {
    List<Person> findByPersonNameContainingIgnoreCase(String name);

    List<Person> findByMemberofOrganizationId(int orgId);

    List<Person> findByMemberOfOrganizationIdIn(Collection<Integer> orgIds);

    List<Person> findByManagerPersonId(int managerId);
    
}
