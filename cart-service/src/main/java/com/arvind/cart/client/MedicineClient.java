package com.arvind.cart.client;

import com.arvind.cart.dto.MedicineResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medicine-service")
public interface MedicineClient {

    @GetMapping("/api/medicines/{id}")
    MedicineResponse getMedicine(@PathVariable("id") Long id);
}