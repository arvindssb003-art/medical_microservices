package com.arvind.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${payment.kafka.topics.requested}")
    private String paymentRequestedTopic;

    @Value("${payment.kafka.topics.completed}")
    private String paymentCompletedTopic;

    @Value("${payment.kafka.topics.failed}")
    private String paymentFailedTopic;

    @Bean
    public NewTopic paymentRequestedTopic() {
        return TopicBuilder.name(paymentRequestedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(paymentCompletedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(paymentFailedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}