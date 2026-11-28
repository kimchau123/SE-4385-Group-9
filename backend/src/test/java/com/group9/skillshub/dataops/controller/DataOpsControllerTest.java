package com.group9.skillshub.dataops.controller;

import com.group9.skillshub.dataops.dto.BackupResult;
import com.group9.skillshub.dataops.dto.ImportResult;
import com.group9.skillshub.dataops.dto.ImportValidationResult;
import com.group9.skillshub.dataops.service.DataOpsService;
import com.group9.skillshub.common.ApiExceptionHandler;
import com.group9.skillshub.common.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataOpsController.class)
@Import(ApiExceptionHandler.class)
class DataOpsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataOpsService dataOpsService;

    @Test
    void validatesAndImportsCsvAsAdmin() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file", "skills.csv", "text/csv",
                "skillName,skillDescription,skillType\nJava,Programming language,TECHNOLOGY\n"
                        .getBytes(StandardCharsets.UTF_8));
        when(dataOpsService.validateImportedData(any(byte[].class)))
                .thenReturn(new ImportValidationResult(true, 1, java.util.List.of()));
        when(dataOpsService.importSkillsCatalog(any(byte[].class)))
                .thenReturn(new ImportResult(1, 1, "IMPORTED", "Imported 1 skills", Instant.now()));

        mockMvc.perform(multipart("/api/dataops/validate").file(csv)
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        mockMvc.perform(multipart("/api/dataops/import").file(csv)
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedCount").value(1));
    }

    @Test
    void rejectsEmptyCsvPayload() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/dataops/import").file(empty)
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void exportsInventoryReportAsCsv() throws Exception {
        byte[] report = "organizationId,personId\n2,3\n".getBytes(StandardCharsets.UTF_8);
        when(dataOpsService.exportInventoryReport(2)).thenReturn(report);

        mockMvc.perform(get("/api/dataops/report").param("orgId", "2")
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"inventory-org-2.csv\""))
                .andExpect(content().bytes(report));
    }

    @Test
    void createsAndRestoresBackupAsAdmin() throws Exception {
        when(dataOpsService.createBackup())
                .thenReturn(new BackupResult(3, Instant.now()));

        mockMvc.perform(post("/api/dataops/backups")
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backupId").value(3));
        mockMvc.perform(post("/api/dataops/backups/3/restore")
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void returnsStructuredNotFoundForUnknownImportOrReport() throws Exception {
        when(dataOpsService.getImportedResults(404))
                .thenThrow(new ResourceNotFoundException("Import 404 was not found"));
        mockMvc.perform(get("/api/dataops/imports/404")
                        .with(user("1").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
