package com.arvind.medicine.service;

import com.arvind.medicine.client.InventoryClient;
import com.arvind.medicine.repository.MedicineImportJobRepository;
import com.arvind.medicine.repository.MedicineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedicineImportServiceTest {

    private final MedicineImportJobRepository importJobRepository =
            mock(MedicineImportJobRepository.class);
    private final MedicineRepository medicineRepository =
            mock(MedicineRepository.class);
    private final MedicineService medicineService = mock(MedicineService.class);
    private final InventoryClient inventoryClient = mock(InventoryClient.class);

    private final MedicineImportService service = new MedicineImportService(
            importJobRepository,
            medicineRepository,
            medicineService,
            inventoryClient,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            Validation.buildDefaultValidatorFactory().getValidator()
    );

    @Test
    void previewsValidCsvRowsForAdminConfirmation() {
        when(medicineRepository.existsByNameIgnoreCase("Example Medicine"))
                .thenReturn(false);

        var preview = service.preview(csv("""
                name,genericName,manufacturer,category,description,price,prescriptionRequired,expiryDate,active,stockQuantity,reorderLevel
                Example Medicine,Example Ingredient,Example Pharma,ANALGESIC,For pain relief,25.50,false,%s,true,100,10
                """.formatted(LocalDate.now().plusDays(30))));

        assertEquals(1, preview.validCount());
        assertEquals(0, preview.errorCount());
        assertEquals("Example Medicine", preview.rows().getFirst().medicine().getName());
        assertEquals(100, preview.rows().getFirst().stockQuantity());
    }

    @Test
    void marksInvalidRowsInsteadOfSilentlyImportingThem() {
        var preview = service.preview(csv("""
                name,genericName,manufacturer,category,description,price,prescriptionRequired,expiryDate,active,stockQuantity,reorderLevel
                Broken,,,NOT_A_CATEGORY,nope,-2,no,%s,yes,-1,invalid
                """.formatted(LocalDate.now().minusDays(1))));

        assertEquals(0, preview.validCount());
        assertEquals(1, preview.errorCount());
        assertFalse(preview.rows().getFirst().errors().isEmpty());
    }

    @Test
    void previewsValidExcelRowsIncludingDateCells() throws IOException {
        when(medicineRepository.existsByNameIgnoreCase("Excel Medicine"))
                .thenReturn(false);
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Medicines");
            var headers = new String[]{
                    "name", "genericName", "manufacturer", "category", "description",
                    "price", "prescriptionRequired", "expiryDate", "active",
                    "stockQuantity", "reorderLevel"
            };
            var header = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                header.createCell(index).setCellValue(headers[index]);
            }
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("Excel Medicine");
            row.createCell(1).setCellValue("Example Ingredient");
            row.createCell(2).setCellValue("Example Pharma");
            row.createCell(3).setCellValue("ANALGESIC");
            row.createCell(4).setCellValue("For pain relief");
            row.createCell(5).setCellValue(25.50);
            row.createCell(6).setCellValue(false);
            var expiryCell = row.createCell(7);
            expiryCell.setCellValue(Date.from(LocalDate.now().plusDays(30)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
            expiryCell.setCellStyle(dateStyle);
            row.createCell(8).setCellValue(true);
            row.createCell(9).setCellValue(100);
            row.createCell(10).setCellValue(10);
            workbook.write(output);
            workbookBytes = output.toByteArray();
        }
        var file = new MockMultipartFile(
                "file",
                "medicines.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookBytes
        );

        var preview = service.preview(file);

        assertEquals(1, preview.validCount());
        assertEquals(0, preview.errorCount());
    }

    @Test
    void rejectsUnsupportedPdfUploads() {
        var file = new MockMultipartFile(
                "file",
                "medicines.pdf",
                "application/pdf",
                "not a spreadsheet".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(IllegalArgumentException.class, () -> service.preview(file));
    }

    private MockMultipartFile csv(String data) {
        return new MockMultipartFile(
                "file",
                "medicines.csv",
                "text/csv",
                data.getBytes(StandardCharsets.UTF_8)
        );
    }
}
