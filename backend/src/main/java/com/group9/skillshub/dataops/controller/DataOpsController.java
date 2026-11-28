package com.group9.skillshub.dataops.controller;

import com.group9.skillshub.dataops.dto.BackupResult;
import com.group9.skillshub.dataops.dto.ImportResult;
import com.group9.skillshub.dataops.service.DataOpsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/dataops")
public class DataOpsController {

    private final DataOpsService dataOpsService;

    public DataOpsController(DataOpsService dataOpsService) {
        this.dataOpsService = dataOpsService;
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateImportedData(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(dataOpsService.validateImportedData(readCsvFile(file)));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSkills(@RequestParam("file") MultipartFile file) {
        ImportResult result = dataOpsService.importSkillsCatalog(readCsvFile(file));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/imports/{importId}")
    public ResponseEntity<?> getImportResult(@PathVariable long importId) {
        return ResponseEntity.ok(dataOpsService.getImportedResults(importId));
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadReport(@RequestParam int orgId) {
        byte[] report = dataOpsService.exportInventoryReport(orgId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"inventory-org-" + orgId + ".csv\"");
        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }

    @PostMapping("/backups")
    public ResponseEntity<?> createBackup() {
        BackupResult result = dataOpsService.createBackup();
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/backups/{backupId}/restore")
    public ResponseEntity<?> restoreBackup(@PathVariable long backupId) {
        dataOpsService.restoreBackup(backupId);
        return ResponseEntity.ok().build();
    }

    private byte[] readCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("CSV file could not be read", exception);
        }
    }
}
