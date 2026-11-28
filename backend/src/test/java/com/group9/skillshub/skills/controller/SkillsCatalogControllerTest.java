package com.group9.skillshub.skills.controller;

import com.group9.skillshub.common.ApiExceptionHandler;
import com.group9.skillshub.common.ResourceConflictException;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.security.SecurityConfig;
import com.group9.skillshub.skills.dto.ProficiencyLevelRequest;
import com.group9.skillshub.skills.dto.SkillRequest;
import com.group9.skillshub.skills.model.Skill;
import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.service.SkillsCatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// HTTP tests with real validation, error handling and security, and a mocked service; runs as admin unless a test says otherwise.
@WebMvcTest(SkillsCatalogController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class})
@WithMockUser(roles = "ADMIN")
class SkillsCatalogControllerTest {

    private static final String DESCRIPTION = "A general-purpose programming language.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillsCatalogService catalogService;

    private static Skill skill(int id, String name, ApprovalStatus status) {
        Skill skill = new Skill();
        skill.setSkillId(id);
        skill.setSkillName(name);
        skill.setSkillType(SkillType.TECHNOLOGY);
        skill.setStatus(status);
        return skill;
    }

    private ResultActions postSkill(String name, String description) throws Exception {
        String descriptionJson = description == null ? "null" : "\"" + description + "\"";
        return mockMvc.perform(post("/api/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"skillName": "%s", "skillDescription": %s, "skillType": "TECHNOLOGY"}
                        """.formatted(name, descriptionJson)));
    }

    // TC-WP-01, TC-WP-02

    @Test
    void createSkillReturns201WithRequestedStatus() throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenReturn(skill(13, "Python", ApprovalStatus.REQUESTED));

        postSkill("Python", DESCRIPTION)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skillId").value(13))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50})
    void createSkillAcceptsNamesAtTheLengthLimits(int length) throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenReturn(skill(13, "a".repeat(length), ApprovalStatus.REQUESTED));

        postSkill("a".repeat(length), null).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 51})
    void createSkillRejectsNamesOutsideTheLengthLimits(int length) throws Exception {
        postSkill("a".repeat(length), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(catalogService, never()).addSkill(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 500})
    void createSkillAcceptsDescriptionsAtTheLengthLimits(int length) throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenReturn(skill(13, "Python", ApprovalStatus.REQUESTED));

        postSkill("Python", "d".repeat(length)).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 501})
    void createSkillRejectsDescriptionsOutsideTheLengthLimits(int length) throws Exception {
        postSkill("Python", "d".repeat(length))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(catalogService, never()).addSkill(any());
    }

    @Test
    void createSkillAcceptsEmptyDescriptionFromForm() throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenReturn(skill(13, "Python", ApprovalStatus.REQUESTED));

        postSkill("Python", "").andExpect(status().isCreated());
        verify(catalogService).addSkill(new SkillRequest("Python", null, SkillType.TECHNOLOGY));
    }

    @Test
    void createSkillRejectsMissingType() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillName": "Python"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSkillReturns409WhenServiceReportsDuplicate() throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenThrow(new ResourceConflictException("A skill named 'Java' already exists"));

        postSkill("Java", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A skill named 'Java' already exists"));
    }

    // TC-WP-03

    @Test
    void getSkillsWithoutParametersReturnsAll() throws Exception {
        when(catalogService.getAllSkills()).thenReturn(List.of(skill(1, "Java", ApprovalStatus.APPROVED)));

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillName").value("Java"));
    }

    @Test
    void getSkillsWithOnlyQuerySearchesByName() throws Exception {
        when(catalogService.searchSkills("dat")).thenReturn(List.of());

        mockMvc.perform(get("/api/skills").param("query", "dat")).andExpect(status().isOk());
        verify(catalogService).searchSkills("dat");
    }

    @Test
    void getSkillsPassesFiltersToService() throws Exception {
        when(catalogService.filterSkills(null, SkillType.TECHNOLOGY, ApprovalStatus.APPROVED))
                .thenReturn(List.of(skill(1, "Java", ApprovalStatus.APPROVED)));

        mockMvc.perform(get("/api/skills").param("type", "TECHNOLOGY").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillId").value(1));
    }

    @Test
    void getSkillsRejectsUnknownTypeWith400() throws Exception {
        mockMvc.perform(get("/api/skills").param("type", "TECH"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value 'TECH' for parameter 'type'"));
    }

    @Test
    void getSkillByIdReturnsSkill() throws Exception {
        when(catalogService.getSkillById(1)).thenReturn(skill(1, "Java", ApprovalStatus.APPROVED));

        mockMvc.perform(get("/api/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillName").value("Java"));
    }

    @Test
    void getSkillByIdReturns404WhenMissing() throws Exception {
        when(catalogService.getSkillById(99)).thenThrow(new ResourceNotFoundException("Skill 99 was not found"));

        mockMvc.perform(get("/api/skills/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateSkillReturnsUpdatedSkill() throws Exception {
        when(catalogService.updateSkill(eq(1), any(SkillRequest.class)))
                .thenReturn(skill(1, "Java 21", ApprovalStatus.APPROVED));

        mockMvc.perform(put("/api/skills/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillName": "Java 21", "skillType": "TECHNOLOGY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillName").value("Java 21"));
    }

    @Test
    void deleteSkillReturns204() throws Exception {
        mockMvc.perform(delete("/api/skills/13")).andExpect(status().isNoContent());
        verify(catalogService).deleteSkill(13);
    }

    @Test
    void deleteSkillInUseReturns409() throws Exception {
        doThrow(new ResourceConflictException("in use")).when(catalogService).deleteSkill(1);

        mockMvc.perform(delete("/api/skills/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // TC-WP-04, TC-OPS-05

    @Test
    void approveReturns200ForAdmin() throws Exception {
        when(catalogService.approveSkill(13)).thenReturn(skill(13, "Python", ApprovalStatus.APPROVED));

        mockMvc.perform(put("/api/skills/13/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void denyReturns200ForAdmin() throws Exception {
        when(catalogService.denySkill(13)).thenReturn(skill(13, "Python", ApprovalStatus.CANCELED));

        mockMvc.perform(put("/api/skills/13/deny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void approveAndDenyReturn403ForNonAdmin() throws Exception {
        mockMvc.perform(put("/api/skills/13/approve")).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/skills/13/deny")).andExpect(status().isForbidden());
        verify(catalogService, never()).approveSkill(13);
        verify(catalogService, never()).denySkill(13);
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminCanStillCreateSkillRequests() throws Exception {
        when(catalogService.addSkill(any(SkillRequest.class)))
                .thenReturn(skill(13, "Python", ApprovalStatus.REQUESTED));

        postSkill("Python", null).andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    void requestsWithoutLoginReturn401() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // TC-WP-05

    @Test
    void getProficiencyLevelsReturnsList() throws Exception {
        SkillProficiencyLevel beginner = new SkillProficiencyLevel();
        beginner.setSkillProficiencyLevelId(1);
        beginner.setSkillProficiencyLevelName("Beginner");
        when(catalogService.getProficiencyLevels()).thenReturn(List.of(beginner));

        mockMvc.perform(get("/api/skills/proficiency-levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillProficiencyLevelName").value("Beginner"));
    }

    @Test
    void renameProficiencyLevelReturnsRenamedLevel() throws Exception {
        SkillProficiencyLevel renamed = new SkillProficiencyLevel();
        renamed.setSkillProficiencyLevelId(2);
        renamed.setSkillProficiencyLevelName("Novice");
        when(catalogService.updateProficiencyLevel(2, new ProficiencyLevelRequest("Novice"))).thenReturn(renamed);

        mockMvc.perform(put("/api/skills/proficiency-levels/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillProficiencyLevelName": "Novice"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillProficiencyLevelId").value(2))
                .andExpect(jsonPath("$.skillProficiencyLevelName").value("Novice"));
    }

    @Test
    void renameProficiencyLevelRejectsBlankName() throws Exception {
        mockMvc.perform(put("/api/skills/proficiency-levels/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillProficiencyLevelName": " "}
                                """))
                .andExpect(status().isBadRequest());
        verify(catalogService, never()).updateProficiencyLevel(eq(2), any());
    }
}
