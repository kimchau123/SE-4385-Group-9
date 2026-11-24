package com.group9.skillshub.inventory.controller;

import com.group9.skillshub.common.ApiExceptionHandler;
import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.inventory.model.PersonSkill;
import com.group9.skillshub.inventory.service.SkillsInventoryService;
import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.projects.model.PersonProjectAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillsInventoryController.class)
@Import(ApiExceptionHandler.class)
class SkillsInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillsInventoryService inventoryService;

    @Test
    void createInventorySkillReturnsCreatedAndRequested() throws Exception {
        PersonSkill skill = personSkill(3, 10, 2, ApprovalStatus.REQUESTED);
        when(inventoryService.assignSkillToPerson(3, 10, 2)).thenReturn(skill);

        mockMvc.perform(post("/api/inventory/persons/3/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillId": 10, "levelId": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.personId").value(3));
    }

    @Test
    void updateProficiencyReturnsUpdatedRequestedSkill() throws Exception {
        when(inventoryService.updatePersonSkill(3, 10, 5))
                .thenReturn(personSkill(3, 10, 5, ApprovalStatus.REQUESTED));

        mockMvc.perform(put("/api/inventory/proficiency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": 3, "skillId": 10, "levelId": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillProficiencyLevelId").value(5))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void invalidProficiencyRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/inventory/proficiency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": 3, "skillId": 10, "levelId": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void proficiencyLevelAboveFiveReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/inventory/proficiency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": 3, "skillId": 10, "levelId": 6}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void approveProficiencyRequest() throws Exception {
        when(inventoryService.updatePersonSkillStatus(3, 10, ApprovalStatus.APPROVED))
                .thenReturn(personSkill(3, 10, 2, ApprovalStatus.APPROVED));

        mockMvc.perform(put("/api/inventory/proficiency/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": 3, "skillId": 10, "status": "APPROVED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void requestAssignmentReturnsCreated() throws Exception {
        PersonProjectAssignment assignment = new PersonProjectAssignment();
        assignment.setPersonId(3);
        assignment.setProjectId(22);
        assignment.setStatus(ApprovalStatus.REQUESTED);
        when(inventoryService.requestAssignment(3, 22)).thenReturn(assignment);

        mockMvc.perform(post("/api/inventory/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personId": 3, "projectId": 22}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.projectId").value(22));
    }

    @Test
    void inventoryReturnsStructuredNotFoundWhenPersonIsMissing() throws Exception {
        when(inventoryService.getSkillsByPerson(404))
                .thenThrow(new ResourceNotFoundException("Person 404 was not found"));

        mockMvc.perform(get("/api/inventory/persons/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Person 404 was not found"));
    }

    private PersonSkill personSkill(int personId, int skillId, int levelId, ApprovalStatus status) {
        PersonSkill personSkill = new PersonSkill();
        personSkill.setPersonId(personId);
        personSkill.setSkillId(skillId);
        personSkill.setSkillProficiencyLevelId(levelId);
        personSkill.setStatus(status);
        return personSkill;
    }
}
