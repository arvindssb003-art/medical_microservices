package com.arvind.medicine.service;

import com.arvind.medicine.client.InventoryClient;
import com.arvind.medicine.dto.MedicineImportCommitResponse;
import com.arvind.medicine.dto.MedicineImportPreviewResponse;
import com.arvind.medicine.dto.MedicineImportRow;
import com.arvind.medicine.dto.MedicineRequest;
import com.arvind.medicine.entity.MedicineCategory;
import com.arvind.medicine.entity.MedicineImportJob;
import com.arvind.medicine.exception.MedicineImportFailedException;
import com.arvind.medicine.repository.MedicineImportJobRepository;
import com.arvind.medicine.repository.MedicineRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineImportService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> REQUIRED_COLUMNS = List.of(
            "name",
            "genericname",
            "manufacturer",
            "category",
            "price",
            "prescriptionrequired",
            "expirydate",
            "active",
            "stockquantity",
            "reorderlevel"
    );

    private final MedicineImportJobRepository importJobRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineService medicineService;
    private final InventoryClient inventoryClient;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Transactional
    public MedicineImportPreviewResponse preview(MultipartFile file) {
        validateFile(file);

        List<Map<String, String>> records = parseFile(file);
        if (records.isEmpty()) {
            throw new IllegalArgumentException("The upload contains no medicine rows");
        }

        List<MedicineImportRow> rows = validateRows(records);
        int validCount = (int) rows.stream().filter(row -> row.errors().isEmpty()).count();
        String importId = UUID.randomUUID().toString();
        MedicineImportPreviewResponse preview = new MedicineImportPreviewResponse(
                importId,
                validCount,
                rows.size() - validCount,
                rows
        );
        try {
            importJobRepository.save(new MedicineImportJob(
                    importId,
                    objectMapper.writeValueAsString(preview)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not persist the medicine import preview", exception);
        }
        return preview;
    }

    @Transactional
    public MedicineImportCommitResponse commit(String importId) {
        MedicineImportJob job = importJobRepository.findByIdForUpdate(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import preview was not found"));
        if (!"PREVIEWED".equals(job.getStatus())) {
            throw new IllegalArgumentException("This import has already been processed");
        }

        MedicineImportPreviewResponse preview;
        try {
            preview = objectMapper.readValue(
                    job.getPreviewJson(),
                    MedicineImportPreviewResponse.class
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored medicine import preview is invalid", exception);
        }
        if (preview.errorCount() > 0 || preview.validCount() == 0) {
            throw new IllegalArgumentException(
                    "Correct all invalid rows and upload a new file before importing"
            );
        }

        List<Long> createdMedicineIds = new ArrayList<>();
        try {
            for (MedicineImportRow row : preview.rows()) {
                var medicine = medicineService.createMedicine(row.medicine());
                createdMedicineIds.add(medicine.getId());
                inventoryClient.createInventory(new InventoryClient.InventoryCreateRequest(
                        medicine.getId(),
                        row.stockQuantity(),
                        0,
                        row.reorderLevel()
                ));
            }
        } catch (RuntimeException exception) {
            compensateCreatedRecords(createdMedicineIds, exception);
            log.error("Bulk medicine import {} failed", importId, exception);
            throw new MedicineImportFailedException(
                    "Bulk import failed. Created medicine and inventory records were rolled back where possible.",
                    exception
            );
        }

        job.setStatus("COMPLETED");
        return new MedicineImportCommitResponse(
                importId,
                preview.validCount(),
                job.getStatus()
        );
    }

    private void compensateCreatedRecords(List<Long> medicineIds, RuntimeException failure) {
        for (int index = medicineIds.size() - 1; index >= 0; index--) {
            Long medicineId = medicineIds.get(index);
            try {
                inventoryClient.deleteInventory(medicineId);
            } catch (RuntimeException compensationFailure) {
                failure.addSuppressed(compensationFailure);
                log.error("Could not roll back inventory for medicine {}", medicineId, compensationFailure);
            }
            try {
                medicineService.deleteMedicine(medicineId);
            } catch (RuntimeException compensationFailure) {
                failure.addSuppressed(compensationFailure);
                log.error("Could not roll back medicine {}", medicineId, compensationFailure);
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Select a non-empty CSV or Excel file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Medicine import files must be 10 MB or smaller");
        }
        String filename = file.getOriginalFilename();
        if (filename == null
                || !(filename.toLowerCase(Locale.ROOT).endsWith(".csv")
                || filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                || filename.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
            throw new IllegalArgumentException("Only CSV and Excel (.xls/.xlsx) imports are supported");
        }
    }

    private List<Map<String, String>> parseFile(MultipartFile file) {
        String filename = file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            return filename.endsWith(".csv") ? parseCsv(file) : parseWorkbook(file);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the medicine import file", exception);
        }
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.mark(1);
            if (reader.read() != '\uFEFF') {
                reader.reset();
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .get();
            try (CSVParser parser = format.parse(reader)) {
                validateHeaders(parser.getHeaderMap().keySet());
                List<Map<String, String>> rows = new ArrayList<>();
                for (CSVRecord record : parser) {
                    Map<String, String> row = new LinkedHashMap<>();
                    parser.getHeaderMap().keySet().forEach(header ->
                            row.put(normalizeHeader(header), record.get(header).trim())
                    );
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    private List<Map<String, String>> parseWorkbook(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("The Excel file has no worksheets");
            }
            var sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("The Excel file has no header row");
            }

            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Map<Integer, String> headers = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = normalizeHeader(formatter.formatCellValue(cell));
                if (!header.isBlank()) {
                    headers.put(cell.getColumnIndex(), header);
                }
            }
            validateHeaders(headers.values());

            List<Map<String, String>> rows = new ArrayList<>();
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> header : headers.entrySet()) {
                    Cell cell = row.getCell(header.getKey());
                    String value;
                    if ("expirydate".equals(header.getValue())
                            && cell != null
                            && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                            && DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    } else {
                        value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    }
                    values.put(header.getValue(), value);
                }
                if (values.values().stream().anyMatch(value -> !value.isBlank())) {
                    rows.add(values);
                }
            }
            return rows;
        }
    }

    private List<MedicineImportRow> validateRows(List<Map<String, String>> records) {
        List<MedicineImportRow> rows = new ArrayList<>();
        Set<String> uploadedNames = new HashSet<>();
        for (int index = 0; index < records.size(); index++) {
            Map<String, String> record = records.get(index);
            List<String> errors = new ArrayList<>();
            MedicineRequest request = new MedicineRequest();
            parseField(errors, "name", () -> required(record, "name"), request::setName);
            parseField(errors, "genericName",
                    () -> required(record, "genericname"), request::setGenericName);
            parseField(errors, "manufacturer",
                    () -> required(record, "manufacturer"), request::setManufacturer);
            parseField(errors, "category", () -> MedicineCategory.valueOf(
                    required(record, "category").toUpperCase(Locale.ROOT)), request::setCategory);
            request.setDescription(value(record, "description"));
            parseField(errors, "price",
                    () -> new BigDecimal(required(record, "price")), request::setPrice);
            parseField(errors, "prescriptionRequired",
                    () -> parseBoolean(record, "prescriptionrequired"),
                    request::setPrescriptionRequired);
            parseField(errors, "expiryDate",
                    () -> LocalDate.parse(required(record, "expirydate")), request::setExpiryDate);
            parseField(errors, "active",
                    () -> parseBoolean(record, "active"), request::setActive);
            Integer stockQuantity = parseField(errors, "stockQuantity",
                    () -> parseNonNegativeInteger(record, "stockquantity"));
            Integer reorderLevel = parseField(errors, "reorderLevel",
                    () -> parseNonNegativeInteger(record, "reorderlevel"));

            for (ConstraintViolation<MedicineRequest> violation : validator.validate(request)) {
                errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
            }
            if (stockQuantity == null || stockQuantity < 0) {
                errors.add("stockQuantity must be zero or greater");
            }
            if (reorderLevel == null || reorderLevel < 0) {
                errors.add("reorderLevel must be zero or greater");
            }
            String name = request.getName();
            if (name != null && !name.isBlank()) {
                String normalizedName = name.trim().toLowerCase(Locale.ROOT);
                if (!uploadedNames.add(normalizedName)) {
                    errors.add("name is duplicated in this file");
                } else if (medicineRepository.existsByNameIgnoreCase(name.trim())) {
                    errors.add("medicine name already exists");
                }
            }

            rows.add(new MedicineImportRow(
                    index + 2,
                    request,
                    stockQuantity,
                    reorderLevel,
                    errors.stream().distinct().toList()
            ));
        }
        return rows;
    }

    private void validateHeaders(Iterable<String> headers) {
        Set<String> normalized = new HashSet<>();
        headers.forEach(header -> normalized.add(normalizeHeader(header)));
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(column -> !normalized.contains(column))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required columns: " + String.join(", ", missing)
            );
        }
    }

    private String required(Map<String, String> row, String field) {
        String fieldValue = value(row, field);
        if (fieldValue.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return fieldValue;
    }

    private String value(Map<String, String> row, String field) {
        return row.getOrDefault(field, "").trim();
    }

    private boolean parseBoolean(Map<String, String> row, String field) {
        String value = required(row, field);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(field + " must be true or false");
    }

    private Integer parseNonNegativeInteger(Map<String, String> row, String field) {
        String value = required(row, field);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new IllegalArgumentException(field + " must be zero or greater");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(field + " must be a whole number");
        }
    }

    private <T> void parseField(
            List<String> errors,
            String field,
            Supplier<T> parser,
            Consumer<T> setter) {
        T parsed = parseField(errors, field, parser);
        if (parsed != null) {
            setter.accept(parsed);
        }
    }

    private <T> T parseField(List<String> errors, String field, Supplier<T> parser) {
        try {
            return parser.get();
        } catch (IllegalArgumentException exception) {
            errors.add(field + ": " + exception.getMessage());
            return null;
        }
    }

    private String normalizeHeader(String header) {
        return header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
    }
}
