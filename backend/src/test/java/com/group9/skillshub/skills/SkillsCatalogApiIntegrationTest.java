package com.group9.skillshub.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Runs the whole app against an in-memory H2 database, logging in with HTTP Basic.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:skills-catalog-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "auth.demo-password=test-password"
})
@AutoConfigureMockMvc
class SkillsCatalogApiIntegrationTest {

    private static final String PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    private String adminId;
    private String plannerId;

    @BeforeEach
    void createPeople() {
        adminId = String.valueOf(savePerson("Catalog admin", 1, Role.BUSINESS_OPERATIONS));
        plannerId = String.valueOf(savePerson("Workforce planner", 0, Role.WORKFORCE_PLANNER));
    }

    private int savePerson(String name, int isAdmin, Role role) {
        Person person = new Person();
        person.setPersonName(name);
        person.setIsAdmin(isAdmin);
        person.setRole(role);
        return personRepository.save(person).getPersonId();
    }

    private int createSkill(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/skills").with(httpBasic(plannerId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillName": "%s", "skillDescription": "Created by the integration test.",
                                 "skillType": "TECHNOLOGY"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("skillId").asInt();
    }

    @Test
    void plannerCreatesSkillAndAdminApprovesIt() throws Exception {
        int skillId = createSkill("Python");

        mockMvc.perform(get("/api/skills").param("query", "pyth").with(httpBasic(plannerId, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].skillId", hasItem(skillId)));

        // Not approved yet.
        mockMvc.perform(get("/api/skills").param("status", "APPROVED").with(httpBasic(plannerId, PASSWORD)))
                .andExpect(jsonPath("$[*].skillId", not(hasItem(skillId))));

        mockMvc.perform(put("/api/skills/{id}/approve", skillId).with(httpBasic(plannerId, PASSWORD)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/skills/{id}/approve", skillId).with(httpBasic(adminId, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/skills")
                        .param("type", "TECHNOLOGY").param("status", "APPROVED")
                        .with(httpBasic(plannerId, PASSWORD)))
                .andExpect(jsonPath("$[*].skillId", hasItem(skillId)));

        // An approved skill can't be denied.
        mockMvc.perform(put("/api/skills/{id}/deny", skillId).with(httpBasic(adminId, PASSWORD)))
                .andExpect(status().isConflict());

        // Different case is still a duplicate.
        mockMvc.perform(post("/api/skills").with(httpBasic(plannerId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillName": "python", "skillType": "TECHNOLOGY"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void unusedSkillCanBeDeleted() throws Exception {
        int skillId = createSkill("Temporary skill");

        mockMvc.perform(delete("/api/skills/{id}", skillId).with(httpBasic(plannerId, PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/skills/{id}", skillId).with(httpBasic(plannerId, PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void proficiencyLevelsAreSeededAndCanBeRenamed() throws Exception {
        mockMvc.perform(get("/api/skills/proficiency-levels").with(httpBasic(plannerId, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].skillProficiencyLevelName").value("Beginner"))
                .andExpect(jsonPath("$[4].skillProficiencyLevelName").value("Expert"));

        mockMvc.perform(put("/api/skills/proficiency-levels/2").with(httpBasic(plannerId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillProficiencyLevelName": "Novice"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillProficiencyLevelId").value(2))
                .andExpect(jsonPath("$.skillProficiencyLevelName").value("Novice"));

        mockMvc.perform(put("/api/skills/proficiency-levels/2").with(httpBasic(plannerId, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillProficiencyLevelName": "expert"}
                                """))
                .andExpect(status().isConflict());
    }
}
