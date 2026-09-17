package com.arvind.order.client;

import com.arvind.order.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{medicineId}")
    InventoryResponse getInventory(
            @PathVariable("medicineId") Long medicineId
    );
}