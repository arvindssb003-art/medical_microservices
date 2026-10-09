package com.arvind.order.client;

import com.arvind.order.dto.InventoryResponse;
import com.arvind.order.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{medicineId}")
    InventoryResponse getInventory(
            @PathVariable("medicineId") Long medicineId
    );

    @PostMapping("/api/inventory/{medicineId}/decrease")
    InventoryResponse decreaseStock(
            @PathVariable("medicineId") Long medicineId,
            @RequestBody StockUpdateRequest request
    );
}