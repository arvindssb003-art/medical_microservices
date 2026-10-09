package com.arvind.payment.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaConfigTest {

    @Test
    void shouldConfigurePaymentKafkaConsumerForPaymentEvents() {
        KafkaConfig config = new KafkaConfig();
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers(List.of("localhost:9092"));

        ConsumerFactory<String, Object> factory = config.consumerFactory(kafkaProperties);

        assertEquals(List.of("localhost:9092"), factory.getConfigurationProperties().get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("payment-service", factory.getConfigurationProperties().get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("com.arvind.payment.event", factory.getConfigurationProperties().get(JsonDeserializer.TRUSTED_PACKAGES));
        assertEquals(StringDeserializer.class, factory.getConfigurationProperties().get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void shouldEnableKafkaListeners() {
        assertTrue(KafkaConfig.class.isAnnotationPresent(EnableKafka.class));
    }
}
