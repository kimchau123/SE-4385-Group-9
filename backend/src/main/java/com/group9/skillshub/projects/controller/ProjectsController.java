package com.group9.skillshub.projects.controller;

import com.group9.skillshub.projects.dto.RequiredSkillRequest;
import com.group9.skillshub.projects.model.Project;
import com.group9.skillshub.projects.service.ProjectsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectsController {

    private final ProjectsService projectsService;

    public ProjectsController(ProjectsService projectsService) {
        this.projectsService = projectsService;
    }

    @GetMapping
    public ResponseEntity<?> getProjects(@RequestParam(required = false) Integer orgId) {
        return ResponseEntity.ok(orgId == null
                ? projectsService.getProjects()
                : projectsService.getProjectsByOrganization(orgId));
    }

    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectsService.addProject(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable int id, @Valid @RequestBody Project project) {
        return ResponseEntity.ok(projectsService.updateProject(id, project));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelProject(@PathVariable int id) {
        projectsService.cancelProject(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/skills")
    public ResponseEntity<?> getRequiredSkills(@PathVariable int projectId) {
        return ResponseEntity.ok(projectsService.getRequiredSkills(projectId));
    }

    @PostMapping("/{projectId}/skills")
    public ResponseEntity<?> addRequiredSkill(
            @PathVariable int projectId, @Valid @RequestBody RequiredSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectsService.addRequiredSkill(projectId, request.skillId(), request.levelId()));
    }

    @PutMapping("/{projectId}/skills/{skillId}/cancel")
    public ResponseEntity<?> cancelRequiredSkill(
            @PathVariable int projectId, @PathVariable int skillId) {
        projectsService.cancelRequiredSkill(projectId, skillId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/skills/{skillId}")
    public ResponseEntity<?> deleteRequiredSkill(
            @PathVariable int projectId, @PathVariable int skillId) {
        projectsService.cancelRequiredSkill(projectId, skillId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/assignments")
    public ResponseEntity<?> getAssignmentsByProject(@PathVariable int projectId) {
        return ResponseEntity.ok(projectsService.getAssignmentsByProject(projectId));
    }
}
