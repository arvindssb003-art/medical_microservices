package com.arvind.order.client;

import com.arvind.order.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/cart/{userId}")
    CartResponse getCart(
            @PathVariable("userId") String userId
    );
}