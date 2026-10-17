package com.arvind.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    void verifyCustomerOwnsOrder(@PathVariable("orderId") Long orderId);
}
