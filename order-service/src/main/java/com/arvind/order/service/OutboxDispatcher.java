package com.arvind.order.service;

import com.arvind.order.entity.OutboxEvent;
import com.arvind.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void publish(String eventId) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(eventId)
                .orElse(null);
        if (event == null || event.getPublishedAt() != null) {
            return;
        }
        if (event.getNextAttemptAt() != null
                && event.getNextAttemptAt().isAfter(LocalDateTime.now())) {
            return;
        }

        try {
            Object payload = objectMapper.readTree(event.getPayload());
            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload)
                    .get(10, TimeUnit.SECONDS);
            event.setPublishedAt(LocalDateTime.now());
            event.setNextAttemptAt(null);
            event.setLastError(null);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            recordFailure(event, ex);
        } catch (Exception ex) {
            recordFailure(event, ex);
        }
    }

    private void recordFailure(OutboxEvent event, Exception exception) {
        event.setAttempts(event.getAttempts() + 1);
        int exponent = Math.min(event.getAttempts(), 8);
        event.setNextAttemptAt(LocalDateTime.now().plusSeconds(1L << exponent));
        String error = exception.getMessage();
        event.setLastError(error == null ? exception.getClass().getSimpleName() : error);
        log.error(
                "Could not publish outbox event {} to topic {}; retry scheduled at {}",
                event.getId(),
                event.getTopic(),
                event.getNextAttemptAt(),
                exception
        );
    }
}
