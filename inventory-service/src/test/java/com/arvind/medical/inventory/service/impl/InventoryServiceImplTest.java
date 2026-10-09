package com.arvind.medical.inventory.service.impl;

import com.arvind.medical.inventory.dto.InventoryResponse;
import com.arvind.medical.inventory.entity.Inventory;
import com.arvind.medical.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void shouldDecreaseAvailableQuantity_whenStockIsSufficient() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .medicineId(10L)
                .availableQuantity(12)
                .reservedQuantity(0)
                .reorderLevel(2)
                .build();

        when(inventoryRepository.findByMedicineId(10L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.decreaseStock(10L, 3);

        assertEquals(9, response.getAvailableQuantity());
    }

    @Test
    void shouldThrowWhenRequestedQuantityIsMoreThanAvailable() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .medicineId(10L)
                .availableQuantity(4)
                .reservedQuantity(0)
                .reorderLevel(2)
                .build();

        when(inventoryRepository.findByMedicineId(10L))
                .thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.decreaseStock(10L, 5));
    }
}
