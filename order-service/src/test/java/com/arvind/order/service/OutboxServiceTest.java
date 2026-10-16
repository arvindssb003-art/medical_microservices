package com.arvind.order.service;

import com.arvind.order.event.PaymentRequestedEvent;
import com.arvind.order.entity.OutboxEvent;
import com.arvind.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxServiceTest {

    @Test
    void storesPaymentRequestAsJsonOutboxPayload() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxService outboxService = new OutboxService(
                repository,
                new ObjectMapper()
        );
        PaymentRequestedEvent event = PaymentRequestedEvent.builder()
                .orderId(42L)
                .userId("customer-7")
                .amount(new BigDecimal("12.50"))
                .currency("INR")
                .paymentMethod("UPI")
                .build();

        outboxService.enqueue("payment.requested", "42", event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("payment.requested", captor.getValue().getTopic());
        assertEquals("42", captor.getValue().getMessageKey());
        assertEquals(
                "{\"orderId\":42,\"userId\":\"customer-7\",\"amount\":12.50,"
                        + "\"currency\":\"INR\",\"paymentMethod\":\"UPI\"}",
                captor.getValue().getPayload()
        );
    }
}
