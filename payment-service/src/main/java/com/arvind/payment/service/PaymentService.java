package com.arvind.payment.service;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.event.PaymentRequestedEvent;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequestedEvent event);

    PaymentResponse getPaymentById(Long paymentId, String userId);

    PaymentResponse getPaymentByOrderId(Long orderId, String userId);

    PaymentResponse verifyRazorpayPayment(
            Long orderId,
            String userId,
            String razorpayPaymentId,
            String razorpaySignature
    );

    List<PaymentResponse> getAllPayments();
}