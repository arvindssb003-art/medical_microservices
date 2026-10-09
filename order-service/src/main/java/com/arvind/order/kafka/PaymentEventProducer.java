package com.arvind.order.kafka;

import com.arvind.order.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.kafka.topics.requested}")
    private String paymentRequestedTopic;

    public void publishPaymentRequested(PaymentRequestedEvent event) {
        try {
            kafkaTemplate.send(
                    paymentRequestedTopic,
                    event.getOrderId().toString(),
                    event
            ).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted publishing payment request", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Could not publish payment request", ex.getCause());
        }
    }
}
