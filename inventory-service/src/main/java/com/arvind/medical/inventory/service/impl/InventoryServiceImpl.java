package com.arvind.medical.inventory.service.impl;

import com.arvind.medical.inventory.dto.InventoryRequest;
import com.arvind.medical.inventory.dto.InventoryResponse;
import com.arvind.medical.inventory.entity.Inventory;
import com.arvind.medical.inventory.exception.InventoryAlreadyExistsException;
import com.arvind.medical.inventory.exception.InventoryNotFoundException;
import com.arvind.medical.inventory.repository.InventoryRepository;
import com.arvind.medical.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public InventoryResponse createInventory(InventoryRequest request) {

        if (inventoryRepository.existsByMedicineId(request.getMedicineId())) {
            throw new InventoryAlreadyExistsException(
                    "Inventory already exists for medicine ID: "
                            + request.getMedicineId()
            );
        }

        Inventory inventory = Inventory.builder()
                .medicineId(request.getMedicineId())
                .availableQuantity(request.getAvailableQuantity())
                .reservedQuantity(
                        request.getReservedQuantity() != null
                                ? request.getReservedQuantity()
                                : 0
                )
                .reorderLevel(request.getReorderLevel())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        return mapToResponse(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByMedicineId(Long medicineId) {

        Inventory inventory = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for medicine ID: " + medicineId
                ));

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InventoryResponse updateInventory(
            Long medicineId,
            InventoryRequest request) {

        Inventory inventory = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for medicine ID: " + medicineId
                ));

        inventory.setAvailableQuantity(request.getAvailableQuantity());

        if (request.getReservedQuantity() != null) {
            inventory.setReservedQuantity(request.getReservedQuantity());
        }

        inventory.setReorderLevel(request.getReorderLevel());

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    public void deleteInventory(Long medicineId) {

        Inventory inventory = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for medicine ID: " + medicineId
                ));

        inventoryRepository.delete(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {

        return InventoryResponse.builder()
                .id(inventory.getId())
                .medicineId(inventory.getMedicineId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}