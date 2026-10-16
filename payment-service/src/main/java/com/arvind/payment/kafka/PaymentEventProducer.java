package com.arvind.payment.kafka;

import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.arvind.payment.service.OutboxService;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final OutboxService outboxService;

    @Value("${payment.kafka.topics.completed}")
    private String paymentCompletedTopic;

    @Value("${payment.kafka.topics.failed}")
    private String paymentFailedTopic;

    public void enqueuePaymentCompleted(PaymentCompletedEvent event) {
        outboxService.enqueue(paymentCompletedTopic, event.getOrderId().toString(), event);
    }

    public void enqueuePaymentFailed(PaymentFailedEvent event) {
        outboxService.enqueue(paymentFailedTopic, event.getOrderId().toString(), event);
    }
}
