package com.arvind.order.kafka;

import com.arvind.order.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentRequestedEvent> kafkaTemplate;

    @Value("${payment.kafka.topics.requested}")
    private String paymentRequestedTopic;

    public void publishPaymentRequested(PaymentRequestedEvent event) {
        kafkaTemplate.send(
                paymentRequestedTopic,
                event.getOrderId().toString(),
                event
        );
    }
}