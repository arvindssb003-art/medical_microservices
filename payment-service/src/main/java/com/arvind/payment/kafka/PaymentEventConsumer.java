package com.arvind.payment.kafka;

import com.arvind.payment.event.PaymentFailedEvent;
import com.arvind.payment.event.PaymentRequestedEvent;
import com.arvind.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
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
        log.info("Processing payment request for order {}", event.getOrderId());
        try {
            paymentService.processPayment(event);
        } catch (RuntimeException ex) {
            if (!isPermanentPaymentFailure(ex)) {
                throw ex;
            }
            log.warn(
                    "Payment request for order {} was rejected permanently: {}",
                    event.getOrderId(),
                    ex.getMessage()
            );
            paymentEventProducer.enqueuePaymentFailed(PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .reason(ex.getMessage())
                    .build());
        }
    }

    private boolean isPermanentPaymentFailure(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException) {
            return true;
        }
        if (exception instanceof RestClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            return responseException.getStatusCode().is4xxClientError()
                    && status != 401
                    && status != 403
                    && status != 429;
        }
        return false;
    }
}
