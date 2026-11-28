package com.group9.skillshub.projects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.repository.ProjectRepository;
import com.group9.skillshub.organizations.model.Organization;
import com.group9.skillshub.organizations.repository.OrganizationRepository;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.repository.SkillRepository;
import com.group9.skillshub.projects.model.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:projects-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class ProjectsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SkillRepository skillRepository;

    private int organizationId;
    private int firstSkillId;
    private int secondSkillId;

    @BeforeEach
    void setUpProjectReferences() {
        Organization organization = new Organization();
        organization.setOrganizationName("Projects API test organization");
        organizationId = organizationRepository.save(organization).getOrganizationId();

        Skill firstSkill = new Skill();
        firstSkill.setSkillName("Projects API Java");
        firstSkill.setSkillDescription("Java skill for project API integration tests");
        firstSkill.setSkillType(SkillType.TECHNOLOGY);
        firstSkill.setStatus(ApprovalStatus.APPROVED);
        firstSkillId = skillRepository.save(firstSkill).getSkillId();

        Skill secondSkill = new Skill();
        secondSkill.setSkillName("Projects API SQL");
        secondSkill.setSkillDescription("SQL skill for project API integration tests");
        secondSkill.setSkillType(SkillType.TECHNOLOGY);
        secondSkill.setStatus(ApprovalStatus.APPROVED);
        secondSkillId = skillRepository.save(secondSkill).getSkillId();
    }

    @Test
    void createsProjectAndRequiredSkillThenCascadesCancellation() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectName": "Resource planning",
                                  "projectOwningOrganizationId": %d,
                                  "projectStartDate": "2026-01-01",
                                  "projectEndDate": "2026-12-31"
                                }
                                """.formatted(organizationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn();
        int projectId = projectIdFrom(createResult);

        mockMvc.perform(post("/api/projects/{projectId}/skills", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skillId": %d,
                                  "levelId": 2
                                }
                                """.formatted(firstSkillId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(post("/api/projects/{projectId}/skills", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skillId": %d,
                                  "levelId": 3
                                }
                                """.formatted(secondSkillId)))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/projects/{id}/cancel", projectId))
                .andExpect(status().isOk());

        Project canceledProject = projectRepository.findById(projectId).orElseThrow();
        assertEquals(RecordStatus.CANCELED, canceledProject.getStatus());
        MvcResult skillsResult = mockMvc.perform(get("/api/projects/{projectId}/skills", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELED"))
                .andExpect(jsonPath("$[1].status").value("CANCELED"))
                .andReturn();
        List<JsonNode> skills = objectMapper.readValue(
                skillsResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
        assertEquals(2, skills.size());
    }

    private int projectIdFrom(MvcResult result) throws Exception {
        JsonNode project = objectMapper.readTree(result.getResponse().getContentAsString());
        return project.get("projectId").asInt();
    }
}
