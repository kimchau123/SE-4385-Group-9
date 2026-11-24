package com.group9.skillshub.organizations.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.organizations.dto.OrganizationHierarchyDTO;
import com.group9.skillshub.organizations.model.Organization;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.organizations.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PersonRepository personRepository;

    @Captor
    private ArgumentCaptor<Collection<Integer>> orgIdsCaptor;

    private OrganizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrganizationServiceImpl(organizationRepository, personRepository);
    }

    @Test
    void getOrganizationHierarchyNestsOrganizationsUnderTheirManagersOrganization() {
        stubCompanyStructure();

        List<OrganizationHierarchyDTO> roots = service.getOrganizationHierarchy();

        assertEquals(1, roots.size());
        OrganizationHierarchyDTO exec = roots.get(0);
        assertEquals("Exec", exec.organizationName());
        assertEquals(List.of("Engineering", "Sales"),
                exec.children().stream().map(OrganizationHierarchyDTO::organizationName).toList());
        OrganizationHierarchyDTO engineering = exec.children().get(0);
        assertEquals(1, engineering.children().size());
        assertEquals("Data", engineering.children().get(0).organizationName());
        assertTrue(engineering.children().get(0).children().isEmpty());
    }

    @Test
    void getOrganizationHierarchyIncludesManagerAndMemberCount() {
        stubCompanyStructure();

        OrganizationHierarchyDTO exec = service.getOrganizationHierarchy().get(0);

        assertEquals(100, exec.managerPersonId());
        assertEquals("Eve Exec", exec.managerName());
        assertEquals(3, exec.memberCount());
    }

    @Test
    void getOrganizationHierarchyReportsZeroMembersForEmptyOrganization() {
        when(organizationRepository.findAll()).thenReturn(List.of(org(1, "Empty", null)));
        when(personRepository.findAll()).thenReturn(List.of());

        OrganizationHierarchyDTO node = service.getOrganizationHierarchy().get(0);

        assertEquals(0, node.memberCount());
        assertNull(node.managerPersonId());
        assertNull(node.managerName());
    }

    @Test
    void getOrganizationHierarchyReturnsEmptyListWhenNoOrganizationsExist() {
        when(organizationRepository.findAll()).thenReturn(List.of());
        when(personRepository.findAll()).thenReturn(List.of());

        assertTrue(service.getOrganizationHierarchy().isEmpty());
    }

    @Test
    void getOrganizationHierarchyReturnsUnrelatedRootsSortedByName() {
        when(organizationRepository.findAll()).thenReturn(List.of(
                org(1, "zeta", null), org(2, "Alpha", null), org(3, "Mid", null)));
        when(personRepository.findAll()).thenReturn(List.of());

        List<OrganizationHierarchyDTO> roots = service.getOrganizationHierarchy();

        assertEquals(List.of("Alpha", "Mid", "zeta"),
                roots.stream().map(OrganizationHierarchyDTO::organizationName).toList());
    }

    @Test
    void getOrganizationHierarchyTreatsSelfManagedOrganizationAsRoot() {
        when(organizationRepository.findAll()).thenReturn(List.of(org(1, "Exec", 100)));
        when(personRepository.findAll()).thenReturn(List.of(person(100, "Eve Exec", 1)));

        List<OrganizationHierarchyDTO> roots = service.getOrganizationHierarchy();

        assertEquals(1, roots.size());
        assertTrue(roots.get(0).children().isEmpty());
    }

    @Test
    void getOrganizationHierarchyTreatsUnknownManagerAsRootWithoutManagerName() {
        when(organizationRepository.findAll()).thenReturn(List.of(org(1, "Orphan", 999)));
        when(personRepository.findAll()).thenReturn(List.of());

        OrganizationHierarchyDTO node = service.getOrganizationHierarchy().get(0);

        assertEquals(999, node.managerPersonId());
        assertNull(node.managerName());
    }

    @Test
    void getOrganizationHierarchyTreatsManagerInMissingOrganizationAsRoot() {
        when(organizationRepository.findAll()).thenReturn(List.of(org(1, "Lonely", 100)));
        when(personRepository.findAll()).thenReturn(List.of(person(100, "Nomad", 77)));

        List<OrganizationHierarchyDTO> roots = service.getOrganizationHierarchy();

        assertEquals(1, roots.size());
        assertEquals("Lonely", roots.get(0).organizationName());
    }

    @Test
    void getOrganizationHierarchyDoesNotDropOrganizationsInAManagementCycle() {
        // Alpha is managed by someone in Beta, and Beta by someone in Alpha.
        when(organizationRepository.findAll()).thenReturn(List.of(
                org(1, "Alpha", 10), org(2, "Beta", 20)));
        when(personRepository.findAll()).thenReturn(List.of(
                person(10, "In Beta", 2), person(20, "In Alpha", 1)));

        List<OrganizationHierarchyDTO> roots = service.getOrganizationHierarchy();

        assertEquals(1, roots.size());
        assertEquals("Alpha", roots.get(0).organizationName());
        assertEquals(1, roots.get(0).children().size());
        assertEquals("Beta", roots.get(0).children().get(0).organizationName());
        assertTrue(roots.get(0).children().get(0).children().isEmpty());
    }

    @Test
    void getOrganizationSubtreeReturnsOnlyTheRequestedBranch() {
        stubCompanyStructure();

        OrganizationHierarchyDTO engineering = service.getOrganizationSubtree(2);

        assertEquals("Engineering", engineering.organizationName());
        assertEquals(1, engineering.children().size());
        assertEquals("Data", engineering.children().get(0).organizationName());
    }

    @Test
    void getOrganizationSubtreeReturnsNotFoundWhenOrganizationDoesNotExist() {
        stubCompanyStructure();

        assertThrows(ResourceNotFoundException.class, () -> service.getOrganizationSubtree(404));
    }

    @Test
    void getOrganizationMembersReturnsMembersSortedByNameIgnoringCase() {
        when(organizationRepository.existsById(7)).thenReturn(true);
        when(personRepository.findByMemberofOrganizationId(7)).thenReturn(List.of(
                person(1, "Zed", 7), person(2, "ann", 7), person(3, "Bob", 7)));

        List<Person> members = service.getOrganizationMembers(7);

        assertEquals(List.of("ann", "Bob", "Zed"), names(members));
    }

    @Test
    void getOrganizationMembersReturnsNotFoundWhenOrganizationDoesNotExist() {
        when(organizationRepository.existsById(404)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getOrganizationMembers(404));
        verify(personRepository, never()).findByMemberofOrganizationId(404);
    }

    @Test
    void getOrganizationMembersIncludingSubOrganizationsQueriesWholeSubtree() {
        stubCompanyStructure();
        when(personRepository.findByMemberOfOrganizationIdIn(orgIdsCaptor.capture())).thenReturn(List.of(
                person(105, "Fay Dev", 3), person(102, "Dan Data", 2)));

        List<Person> members = service.getOrganizationMembersIncludingSubOrganizations(2);

        assertEquals(Set.of(2, 3), Set.copyOf(orgIdsCaptor.getValue()));
        assertEquals(List.of("Dan Data", "Fay Dev"), names(members));
    }

    @Test
    void getOrganizationMembersIncludingSubOrganizationsForLeafQueriesOnlyThatOrganization() {
        stubCompanyStructure();
        when(personRepository.findByMemberOfOrganizationIdIn(orgIdsCaptor.capture())).thenReturn(List.of());

        service.getOrganizationMembersIncludingSubOrganizations(3);

        assertEquals(Set.of(3), Set.copyOf(orgIdsCaptor.getValue()));
    }

    @Test
    void getOrganizationMembersIncludingSubOrganizationsReturnsNotFoundWhenOrganizationDoesNotExist() {
        stubCompanyStructure();

        assertThrows(ResourceNotFoundException.class,
                () -> service.getOrganizationMembersIncludingSubOrganizations(404));
        verify(personRepository, never()).findByMemberOfOrganizationIdIn(org.mockito.ArgumentMatchers.anyCollection());
    }

    /**
     * Exec(1) <- Engineering(2) <- Data(3), and Exec(1) <- Sales(4).
     * An organization's parent is the organization its manager belongs to, so Exec's
     * manager (in Exec itself) makes it the root, and Engineering's manager sits in Exec.
     */
    private void stubCompanyStructure() {
        when(organizationRepository.findAll()).thenReturn(List.of(
                org(1, "Exec", 100),
                org(2, "Engineering", 101),
                org(3, "Data", 102),
                org(4, "Sales", 103)));
        when(personRepository.findAll()).thenReturn(List.of(
                person(100, "Eve Exec", 1),
                person(101, "Ellen Eng", 1),
                person(102, "Dan Data", 2),
                person(103, "Sam Sales", 1),
                person(105, "Fay Dev", 3)));
    }

    private List<String> names(List<Person> people) {
        return people.stream().map(Person::getPersonName).toList();
    }

    private Organization org(int id, String name, Integer managerPersonId) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        organization.setOrganizationName(name);
        organization.setOrganizationManagerPersonId(managerPersonId);
        return organization;
    }

    private Person person(int id, String name, Integer organizationId) {
        Person person = new Person();
        person.setPersonId(id);
        person.setPersonName(name);
        person.setMemberOfOrganizationId(organizationId);
        return person;
    }
}