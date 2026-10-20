package com.arvind.medicine.controller;

import com.arvind.medicine.dto.MedicineImportCommitResponse;
import com.arvind.medicine.dto.MedicineImportPreviewResponse;
import com.arvind.medicine.service.MedicineImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/medicines/import")
@RequiredArgsConstructor
public class MedicineImportController {

    private final MedicineImportService medicineImportService;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MedicineImportPreviewResponse preview(
            @RequestPart("file") MultipartFile file) {
        return medicineImportService.preview(file);
    }

    @PostMapping("/{importId}/commit")
    public MedicineImportCommitResponse commit(@PathVariable String importId) {
        return medicineImportService.commit(importId);
    }
}
