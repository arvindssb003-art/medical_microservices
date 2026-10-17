package com.arvind.delivery.controller;

import com.arvind.delivery.dto.DeliveryRequest;
import com.arvind.delivery.dto.DeliveryResponse;
import com.arvind.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeliveryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deliveryService.createDelivery(request, jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                deliveryService.getDeliveryById(id, jwt.getSubject())
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveryByOrderId(orderId, jwt.getSubject())
        );
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByUserId(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByUserId(jwt.getSubject())
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                deliveryService.updateDeliveryStatus(id, status)
        );
    }
}