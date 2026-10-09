package com.arvind.payment.controller;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.dto.RazorpayPaymentVerificationRequest;
import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.kafka.PaymentEventProducer;
import com.arvind.payment.service.PaymentService;
import jakarta.validation.Valid;
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
    private final PaymentEventProducer paymentEventProducer;

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

    @PostMapping("/order/{orderId}/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            @PathVariable
            @Positive(message = "Order ID must be positive")
            Long orderId,
            @RequestBody @Valid
            RazorpayPaymentVerificationRequest request) {

        PaymentResponse payment = paymentService.verifyRazorpayPayment(
                orderId,
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        paymentEventProducer.publishPaymentCompleted(PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .build());

        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }
}