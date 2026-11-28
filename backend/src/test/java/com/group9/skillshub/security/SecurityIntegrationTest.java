package com.group9.skillshub.security;

import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.model.Role;
import com.group9.skillshub.organizations.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:security-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "auth.demo-password=test-password"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    private static final String PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    private String adminId;
    private String employeeId;

    @BeforeEach
    void createPeople() {
        adminId = String.valueOf(savePerson("Admin user", 1, Role.BUSINESS_OPERATIONS));
        employeeId = String.valueOf(savePerson("Regular employee", 0, Role.EMPLOYEE));
    }

    private int savePerson(String name, int isAdmin, Role role) {
        Person person = new Person();
        person.setPersonName(name);
        person.setIsAdmin(isAdmin);
        person.setRole(role);
        return personRepository.save(person).getPersonId();
    }

    @Test
    void rejectsRequestsWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rejectsWrongPasswordAndUnknownPerson() throws Exception {
        mockMvc.perform(get("/api/projects").with(httpBasic(employeeId, "wrong")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/projects").with(httpBasic("999999", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic(adminId, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personId").value(Integer.parseInt(adminId)))
                .andExpect(jsonPath("$.personName").value("Admin user"))
                .andExpect(jsonPath("$.role").value("BUSINESS_OPERATIONS"))
                .andExpect(jsonPath("$.isAdmin").value(true));
    }

    @Test
    void allowsRegularUserToRead() throws Exception {
        mockMvc.perform(get("/api/projects").with(httpBasic(employeeId, PASSWORD)))
                .andExpect(status().isOk());
    }

    /** TC-OPS-05: a non-admin calling an admin endpoint gets 403. */
    @Test
    void forbidsRegularUserFromAdminActions() throws Exception {
        mockMvc.perform(post("/api/projects").with(httpBasic(employeeId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin access required"));
        mockMvc.perform(put("/api/inventory/proficiency/status").with(httpBasic(employeeId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/dataops/backups").with(httpBasic(employeeId, PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminPastSecurity() throws Exception {
        mockMvc.perform(post("/api/projects").with(httpBasic(adminId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}