package com.arvind.order.service;

import com.arvind.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxDispatcher outboxDispatcher;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay:1000}")
    public void publishPendingEvents() {
        outboxEventRepository
                .findReadyToPublish(LocalDateTime.now(), PageRequest.of(0, 25))
                .forEach(event -> outboxDispatcher.publish(event.getId()));
    }
}
