package com.arvind.delivery.controller;

import com.arvind.delivery.dto.DeliveryRequest;
import com.arvind.delivery.dto.DeliveryResponse;
import com.arvind.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @Valid @RequestBody DeliveryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deliveryService.createDelivery(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                deliveryService.getDeliveryById(id)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveryByOrderId(orderId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByUserId(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByUserId(userId)
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