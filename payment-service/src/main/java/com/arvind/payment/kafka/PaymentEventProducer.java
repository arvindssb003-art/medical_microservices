package com.arvind.payment.kafka;

import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.kafka.topics.completed}")
    private String paymentCompletedTopic;

    @Value("${payment.kafka.topics.failed}")
    private String paymentFailedTopic;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        kafkaTemplate.send(
                paymentCompletedTopic,
                event.getOrderId().toString(),
                event
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(
                paymentFailedTopic,
                event.getOrderId().toString(),
                event
        );
    }
}