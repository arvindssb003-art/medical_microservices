package com.arvind.order.kafka;

import com.arvind.order.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.arvind.order.service.OutboxService;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final OutboxService outboxService;

    @Value("${payment.kafka.topics.requested}")
    private String paymentRequestedTopic;

    public void enqueuePaymentRequested(PaymentRequestedEvent event) {
        outboxService.enqueue(
                paymentRequestedTopic,
                event.getOrderId().toString(),
                event
        );
    }
}
