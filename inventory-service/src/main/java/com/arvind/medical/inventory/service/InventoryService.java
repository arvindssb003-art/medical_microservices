package com.arvind.medical.inventory.service;

import com.arvind.medical.inventory.dto.InventoryRequest;
import com.arvind.medical.inventory.dto.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse createInventory(InventoryRequest request);

    InventoryResponse getInventoryByMedicineId(Long medicineId);

    List<InventoryResponse> getAllInventory();

    InventoryResponse updateInventory(Long medicineId, InventoryRequest request);

    void deleteInventory(Long medicineId);
}