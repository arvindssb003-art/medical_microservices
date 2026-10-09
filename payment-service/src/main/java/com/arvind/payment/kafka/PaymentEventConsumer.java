package com.arvind.payment.kafka;

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
            groupId = "payment-service",
            properties = {
                    "spring.json.value.default.type=com.arvind.payment.event.PaymentRequestedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void consumePaymentRequest(PaymentRequestedEvent event) {

        try {
            paymentService.processPayment(event);
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
