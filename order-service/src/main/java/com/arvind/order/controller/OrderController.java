package com.arvind.order.controller;

import com.arvind.order.dto.CreateOrderRequest;
import com.arvind.order.dto.OrderResponse;
import com.arvind.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(jwt.getSubject(), request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrder(jwt.getSubject(), orderId)
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                orderService.getUserOrders(jwt.getSubject())
        );
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        orderService.cancelOrder(jwt.getSubject(), orderId);

        return ResponseEntity.noContent().build();
    }
}