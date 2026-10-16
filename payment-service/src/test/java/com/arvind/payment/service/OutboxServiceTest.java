package com.arvind.payment.service;

import com.arvind.payment.event.PaymentCompletedEvent;
import com.arvind.payment.entity.OutboxEvent;
import com.arvind.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxServiceTest {

    @Test
    void storesPaymentCompletionAsJsonOutboxPayload() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxService outboxService = new OutboxService(
                repository,
                new ObjectMapper()
        );
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(13L)
                .orderId(42L)
                .userId("customer-7")
                .amount(new BigDecimal("12.50"))
                .currency("INR")
                .transactionId("pay-9")
                .build();

        outboxService.enqueue("payment.completed", "42", event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("payment.completed", captor.getValue().getTopic());
        assertEquals("42", captor.getValue().getMessageKey());
        assertEquals(
                "{\"paymentId\":13,\"orderId\":42,\"userId\":\"customer-7\","
                        + "\"amount\":12.50,\"currency\":\"INR\","
                        + "\"transactionId\":\"pay-9\"}",
                captor.getValue().getPayload()
        );
    }
}
