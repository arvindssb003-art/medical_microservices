package com.arvind.payment.service;

import com.arvind.payment.entity.OutboxEvent;
import com.arvind.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueue(String topic, String messageKey, Object payload) {
        try {
            outboxEventRepository.save(new OutboxEvent(
                    topic,
                    messageKey,
                    objectMapper.writeValueAsString(payload)
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize Kafka outbox event", ex);
        }
    }
}
