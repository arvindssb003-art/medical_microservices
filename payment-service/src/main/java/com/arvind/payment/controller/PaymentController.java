package com.arvind.payment.controller;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.entity.PaymentStatus;
import com.arvind.payment.service.PaymentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable
            @Positive(message = "Order ID must be positive")
            Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId)
        );
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId,
            @RequestParam PaymentStatus status) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(paymentId, status)
        );
    }
}