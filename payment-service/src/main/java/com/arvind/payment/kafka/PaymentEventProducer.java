package com.arvind.payment.kafka;

import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.kafka.topics.completed}")
    private String paymentCompletedTopic;

    @Value("${payment.kafka.topics.failed}")
    private String paymentFailedTopic;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        sendAndAwait(paymentCompletedTopic, event.getOrderId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        sendAndAwait(paymentFailedTopic, event.getOrderId().toString(), event);
    }

    private void sendAndAwait(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted publishing payment event", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Could not publish payment event to " + topic, ex.getCause());
        }
    }
}
