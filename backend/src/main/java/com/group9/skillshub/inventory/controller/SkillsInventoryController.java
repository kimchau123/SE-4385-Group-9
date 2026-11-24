package com.group9.skillshub.inventory.controller;

import com.group9.skillshub.inventory.dto.AssignmentRequest;
import com.group9.skillshub.inventory.dto.AssignmentStatusRequest;
import com.group9.skillshub.inventory.dto.PersonSkillRequest;
import com.group9.skillshub.inventory.dto.PersonSkillStatusRequest;
import com.group9.skillshub.inventory.dto.ProficiencyUpdateRequest;
import com.group9.skillshub.inventory.service.SkillsInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class SkillsInventoryController {

    private final SkillsInventoryService inventoryService;

    public SkillsInventoryController(SkillsInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<?> getPersonInventory(@PathVariable int personId) {
        return ResponseEntity.ok(inventoryService.getSkillsByPerson(personId));
    }

    @PostMapping("/persons/{personId}/skills")
    public ResponseEntity<?> assignSkillToPerson(
            @PathVariable int personId, @Valid @RequestBody PersonSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.assignSkillToPerson(personId, request.skillId(), request.levelId()));
    }

    @PutMapping("/proficiency")
    public ResponseEntity<?> updateProficiency(@Valid @RequestBody ProficiencyUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updatePersonSkill(
                request.personId(), request.skillId(), request.levelId()));
    }

    @PutMapping("/proficiency/status")
    public ResponseEntity<?> updateProficiencyStatus(
            @Valid @RequestBody PersonSkillStatusRequest request) {
        return ResponseEntity.ok(inventoryService.updatePersonSkillStatus(
                request.personId(), request.skillId(), request.status()));
    }

    @GetMapping("/skills/{skillId}")
    public ResponseEntity<?> searchBySkill(@PathVariable int skillId) {
        return ResponseEntity.ok(inventoryService.getPersonsBySkill(skillId));
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> requestAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.requestAssignment(request.personId(), request.projectId()));
    }

    @PutMapping("/assignments/status")
    public ResponseEntity<?> updateAssignmentStatus(
            @Valid @RequestBody AssignmentStatusRequest request) {
        return ResponseEntity.ok(inventoryService.updateAssignmentStatus(
                request.personId(), request.projectId(), request.status()));
    }

    @GetMapping("/persons/{personId}/assignments")
    public ResponseEntity<?> getAssignmentsByPerson(@PathVariable int personId) {
        return ResponseEntity.ok(inventoryService.getAssignmentsByPerson(personId));
    }

    @GetMapping("/projects/{projectId}/assignments")
    public ResponseEntity<?> getAssignmentsByProject(@PathVariable int projectId) {
        return ResponseEntity.ok(inventoryService.getAssignmentsByProject(projectId));
    }
}
