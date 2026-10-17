package com.arvind.order.client;

import com.arvind.order.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/cart/me")
    CartResponse getCart();
}