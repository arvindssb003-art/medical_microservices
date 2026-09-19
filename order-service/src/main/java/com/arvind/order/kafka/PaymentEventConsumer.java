package com.arvind.order.kafka;

import com.arvind.order.entity.Order;
import com.arvind.order.entity.OrderStatus;
import com.arvind.order.event.PaymentCompletedEvent;
import com.arvind.order.event.PaymentFailedEvent;
import com.arvind.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "${payment.kafka.topics.completed}",
            groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.arvind.order.event.PaymentCompletedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void consumePaymentCompleted(
            PaymentCompletedEvent event) {

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            return;
        }

        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.failed}",
            groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.arvind.order.event.PaymentFailedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void consumePaymentFailed(
            PaymentFailedEvent event) {

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
}