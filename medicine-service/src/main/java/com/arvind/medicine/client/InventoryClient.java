package com.arvind.medicine.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory")
    void createInventory(@RequestBody InventoryCreateRequest request);

    @DeleteMapping("/api/inventory/{medicineId}")
    void deleteInventory(@PathVariable("medicineId") Long medicineId);

    record InventoryCreateRequest(
            Long medicineId,
            Integer availableQuantity,
            Integer reservedQuantity,
            Integer reorderLevel) {
    }
}
