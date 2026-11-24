package com.group9.skillshub.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group9.skillshub.organization.model.Person;
import com.group9.skillshub.organization.repository.PersonRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.ProjectRepository;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class SkillsInventoryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void inventoryUpdatesApprovalAndAssignmentWorkThroughApi() throws Exception {
        Person person = new Person();
        person.setPersonName("Inventory test employee");
        Person savedPerson = personRepository.save(person);
        Skill skill = new Skill();
        skill.setSkillName("Inventory test skill");
        skill.setSkillType(com.group9.skillshub.skills.model.SkillType.OTHER);
        skill.setStatus(ApprovalStatus.APPROVED);
        Skill savedSkill = skillRepository.save(skill);
        Project project = new Project();
        project.setProjectName("Inventory test project");
        project.setProjectOwningOrganizationId(1);
        project.setProjectStartDate(LocalDate.of(2026, 1, 1));
        project.setProjectEndDate(LocalDate.of(2026, 12, 31));
        project.setStatus(RecordStatus.APPROVED);
        Project savedProject = projectRepository.save(project);

        mockMvc.perform(post("/api/inventory/persons/{personId}/skills", savedPerson.getPersonId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillId": %d, "levelId": 1}
                                """.formatted(savedSkill.getSkillId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        mockMvc.perform(put("/api/inventory/proficiency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": %d, "skillId": %d, "levelId": 4}
                                """.formatted(savedPerson.getPersonId(), savedSkill.getSkillId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillProficiencyLevelId").value(4))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        mockMvc.perform(put("/api/inventory/proficiency/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": %d, "skillId": %d, "status": "APPROVED"}
                                """.formatted(savedPerson.getPersonId(), savedSkill.getSkillId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/api/inventory/persons/{personId}", savedPerson.getPersonId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillId").value(savedSkill.getSkillId()));

        MvcResult assignmentResult = mockMvc.perform(post("/api/inventory/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": %d, "projectId": %d}
                                """.formatted(savedPerson.getPersonId(), savedProject.getProjectId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        JsonNode assignment = objectMapper.readTree(assignmentResult.getResponse().getContentAsString());
        assertEquals(savedPerson.getPersonId(), assignment.get("personId").asInt());

        mockMvc.perform(put("/api/inventory/assignments/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": %d, "projectId": %d, "status": "APPROVED"}
                                """.formatted(savedPerson.getPersonId(), savedProject.getProjectId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/api/inventory/persons/{personId}/assignments", savedPerson.getPersonId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(savedProject.getProjectId()));
        mockMvc.perform(get("/api/inventory/projects/{projectId}/assignments", savedProject.getProjectId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personId").value(savedPerson.getPersonId()));
        mockMvc.perform(get("/api/inventory/skills/{skillId}", savedSkill.getSkillId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personId").value(savedPerson.getPersonId()));

        mockMvc.perform(put("/api/inventory/proficiency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": %d, "skillId": %d, "levelId": 6}
                                """.formatted(savedPerson.getPersonId(), savedSkill.getSkillId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
