package com.arvind.payment.kafka;

import com.arvind.payment.dto.PaymentResponse;
import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.event.PaymentFailedEvent;
import com.arvind.payment.event.PaymentRequestedEvent;
import com.arvind.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "${payment.kafka.topics.requested}",
            groupId = "payment-service"
    )
    public void consumePaymentRequest(PaymentRequestedEvent event) {

        try {
            PaymentResponse payment = paymentService.processPayment(event);

            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .transactionId(payment.getTransactionId())
                    .build();

            paymentEventProducer.publishPaymentCompleted(completedEvent);

        } catch (Exception ex) {

            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .reason(ex.getMessage())
                    .build();

            paymentEventProducer.publishPaymentFailed(failedEvent);
        }
    }
}