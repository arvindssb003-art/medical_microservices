package com.arvind.medicine.dto;

public record MedicineImportCommitResponse(
        String importId,
        int importedCount,
        String status) {
}
