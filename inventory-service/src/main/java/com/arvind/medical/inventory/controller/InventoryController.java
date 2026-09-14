package com.arvind.medical.inventory.controller;

import com.arvind.medical.inventory.dto.InventoryRequest;
import com.arvind.medical.inventory.dto.InventoryResponse;
import com.arvind.medical.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryRequest request) {

        InventoryResponse response = inventoryService.createInventory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{medicineId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long medicineId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByMedicineId(medicineId)
        );
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    @PutMapping("/{medicineId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long medicineId,
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(medicineId, request)
        );
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable Long medicineId) {

        inventoryService.deleteInventory(medicineId);

        return ResponseEntity.noContent().build();
    }
}