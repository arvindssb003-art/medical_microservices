package com.arvind.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private int attempts;

    private LocalDateTime nextAttemptAt;

    @Column(columnDefinition = "text")
    private String lastError;

    public OutboxEvent(String topic, String messageKey, String payload) {
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
