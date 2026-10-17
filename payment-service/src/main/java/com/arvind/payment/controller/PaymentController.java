package com.arvind.payment.controller;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.dto.RazorpayPaymentVerificationRequest;
import com.arvind.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable
            @Positive(message = "Payment ID must be positive")
            Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId, jwt.getSubject())
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable
            @Positive(message = "Order ID must be positive")
            Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId, jwt.getSubject())
        );
    }

    @PostMapping("/order/{orderId}/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable
            @Positive(message = "Order ID must be positive")
            Long orderId,
            @RequestBody @Valid
            RazorpayPaymentVerificationRequest request) {

        return ResponseEntity.ok(paymentService.verifyRazorpayPayment(
                orderId,
                jwt.getSubject(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        ));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }
}