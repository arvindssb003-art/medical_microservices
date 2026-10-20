package com.arvind.medicine.dto;

import java.util.List;

public record MedicineImportPreviewResponse(
        String importId,
        int validCount,
        int errorCount,
        List<MedicineImportRow> rows) {
}
