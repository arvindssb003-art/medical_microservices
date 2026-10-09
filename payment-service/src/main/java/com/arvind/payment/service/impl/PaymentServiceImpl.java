package com.arvind.payment.service.impl;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.entity.Payment;
import com.arvind.payment.entity.PaymentMethod;
import com.arvind.payment.entity.PaymentStatus;
import com.arvind.payment.event.PaymentRequestedEvent;
import com.arvind.payment.exception.PaymentNotFoundException;
import com.arvind.payment.exception.RazorpayVerificationException;
import com.arvind.payment.gateway.RazorpayGateway;
import com.arvind.payment.repository.PaymentRepository;
import com.arvind.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayGateway razorpayGateway;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequestedEvent event) {

        Payment existingPayment = paymentRepository
                .findByOrderId(event.getOrderId())
                .orElse(null);

        if (existingPayment != null) {
            return mapToResponse(existingPayment);
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(
                event.getPaymentMethod().toUpperCase(Locale.ROOT)
        );

        String razorpayOrderId = razorpayGateway.createOrder(
                event.getOrderId(),
                event.getAmount(),
                event.getCurrency()
        );

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .paymentMethod(paymentMethod)
                .razorpayOrderId(razorpayOrderId)
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );

        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found for order: " + orderId
                        )
                );

        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse verifyRazorpayPayment(
            Long orderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found for order: " + orderId
                        )
                );

        razorpayGateway.verifySignature(
                payment.getRazorpayOrderId(),
                razorpayPaymentId,
                razorpaySignature
        );

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (!razorpayPaymentId.equals(payment.getTransactionId())) {
                throw new RazorpayVerificationException(
                        "Payment has already been verified with a different transaction"
                );
            }
            return mapToResponse(payment);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RazorpayVerificationException(
                    "Payment is not in a verifiable state"
            );
        }

        razorpayGateway.capturePayment(
                razorpayPaymentId,
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                payment.getCurrency()
        );

        payment.setTransactionId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayKeyId(razorpayGateway.getKeyId())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
