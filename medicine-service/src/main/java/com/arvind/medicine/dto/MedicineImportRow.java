package com.arvind.medicine.dto;

import java.util.List;

public record MedicineImportRow(
        int rowNumber,
        MedicineRequest medicine,
        Integer stockQuantity,
        Integer reorderLevel,
        List<String> errors) {
}
