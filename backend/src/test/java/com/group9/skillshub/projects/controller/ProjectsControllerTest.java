package com.group9.skillshub.projects.controller;

import com.group9.skillshub.common.ApiExceptionHandler;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.model.ProjectSkillRequired;
import com.group9.skillshub.projects.model.RecordStatus;
import com.group9.skillshub.projects.service.ProjectsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.group9.skillshub.security.SecurityConfig;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectsController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class})
@WithMockUser(roles = "ADMIN")
class ProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectsService projectsService;

    @Test
    void createProjectReturnsCreatedProjectAndApprovedStatus() throws Exception {
        Project project = new Project();
        project.setProjectId(5);
        project.setProjectName("Skills Hub rollout");
        project.setStatus(RecordStatus.APPROVED);
        project.setProjectOwningOrganizationId(2);
        project.setProjectStartDate(LocalDate.of(2026, 1, 1));
        project.setProjectEndDate(LocalDate.of(2026, 12, 31));
        when(projectsService.addProject(any(Project.class))).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectName": "Skills Hub rollout",
                                  "projectOwningOrganizationId": 2,
                                  "projectStartDate": "2026-01-01",
                                  "projectEndDate": "2026-12-31"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.projectId").value(5));
    }

    @Test
    void createProjectRejectsInvalidPayloadWithStructuredBadRequest() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectName": "",
                                  "projectOwningOrganizationId": 0,
                                  "projectStartDate": "2026-12-31",
                                  "projectEndDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void updateProjectReturnsStructuredNotFoundWhenMissing() throws Exception {
        when(projectsService.updateProject(any(Integer.class), any(Project.class)))
                .thenThrow(new ResourceNotFoundException("Project 404 was not found"));

        mockMvc.perform(put("/api/projects/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectName": "Project",
                                  "projectOwningOrganizationId": 2,
                                  "projectStartDate": "2026-01-01",
                                  "projectEndDate": "2026-12-31"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project 404 was not found"));
    }

    @Test
    void addRequiredSkillReturnsCreatedApprovedRequirement() throws Exception {
        when(projectsService.addRequiredSkill(5, 11, 2))
                .thenReturn(new ProjectSkillRequired(5, 11, 2, RecordStatus.APPROVED));

        mockMvc.perform(post("/api/projects/5/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skillId": 11,
                                  "levelId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(5))
                .andExpect(jsonPath("$.skillId").value(11))
                .andExpect(jsonPath("$.skillProficiencyLevelId").value(2))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void cancelProjectReturnsOk() throws Exception {
        mockMvc.perform(put("/api/projects/5/cancel"))
                .andExpect(status().isOk());
    }
}
