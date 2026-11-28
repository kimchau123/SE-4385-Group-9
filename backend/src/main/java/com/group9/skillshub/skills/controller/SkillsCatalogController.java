package com.group9.skillshub.skills.controller;

import com.group9.skillshub.projects.model.ApprovalStatus;
import com.group9.skillshub.skills.dto.ProficiencyLevelRequest;
import com.group9.skillshub.skills.dto.SkillRequest;
import com.group9.skillshub.skills.model.SkillType;
import com.group9.skillshub.skills.service.SkillsCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST endpoints for the Skills Catalog. All require login while approve/deny are admin-only.
@RestController
@RequestMapping("/api/skills")
public class SkillsCatalogController {

    private final SkillsCatalogService catalogService;

    public SkillsCatalogController(SkillsCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // No params returns all skills; query, type and status can be combined in any way.
    @GetMapping
    public ResponseEntity<?> getSkills(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) SkillType type,
            @RequestParam(required = false) ApprovalStatus status) {
        if (type == null && status == null) {
            return ResponseEntity.ok(query == null
                    ? catalogService.getAllSkills()
                    : catalogService.searchSkills(query));
        }
        return ResponseEntity.ok(catalogService.filterSkills(query, type, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable int id) {
        return ResponseEntity.ok(catalogService.getSkillById(id));
    }

    @PostMapping
    public ResponseEntity<?> createSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addSkill(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable int id, @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(catalogService.updateSkill(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable int id) {
        catalogService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveSkill(@PathVariable int id) {
        return ResponseEntity.ok(catalogService.approveSkill(id));
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<?> denySkill(@PathVariable int id) {
        return ResponseEntity.ok(catalogService.denySkill(id));
    }

    @GetMapping("/proficiency-levels")
    public ResponseEntity<?> getProficiencyLevels() {
        return ResponseEntity.ok(catalogService.getProficiencyLevels());
    }

    // No POST because the scale is fixed at 1-5.
    @PutMapping("/proficiency-levels/{id}")
    public ResponseEntity<?> updateProficiencyLevel(
            @PathVariable int id, @Valid @RequestBody ProficiencyLevelRequest request) {
        return ResponseEntity.ok(catalogService.updateProficiencyLevel(id, request));
    }
}
