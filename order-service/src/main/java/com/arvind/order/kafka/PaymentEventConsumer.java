package com.arvind.order.kafka;
import com.arvind.order.client.InventoryClient;
import com.arvind.order.dto.StockUpdateRequest;
import com.arvind.order.entity.Order;
import com.arvind.order.entity.OrderItem;
import com.arvind.order.entity.OrderStatus;
import com.arvind.order.event.PaymentCompletedEvent;
import com.arvind.order.event.PaymentFailedEvent;
import com.arvind.order.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @PostConstruct
    public void init() {
        System.out.println("🔥 PaymentEventConsumer BEAN CREATED 🔥");
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.completed}",
            groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.arvind.order.event.PaymentCompletedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional
    public void consumePaymentCompleted(PaymentCompletedEvent event) {

        System.out.println(
                "🔥 PAYMENT COMPLETED EVENT RECEIVED: orderId="
                        + event.getOrderId()
                        + ", paymentId="
                        + event.getPaymentId()
        );

        Order order = orderRepository
                .findByIdWithItems(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for payment.completed event: " + event.getOrderId()
                ));

        System.out.println(
                "✅ ORDER FOUND: "
                        + order.getId()
                        + ", status="
                        + order.getStatus()
        );

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            System.out.println(
                    "ℹ️ IGNORING PAYMENT COMPLETED EVENT FOR ORDER STATUS "
                            + order.getStatus() + ": " + order.getId()
            );
            return;
        }

        for (OrderItem item : order.getItems()) {

            System.out.println(
                    "📦 DECREASING INVENTORY: medicineId="
                            + item.getMedicineId()
                            + ", quantity="
                            + item.getQuantity()
            );

            StockUpdateRequest request =
                    new StockUpdateRequest(item.getQuantity());

            try {
                inventoryClient.decreaseStock(
                        item.getMedicineId(),
                        request
                );
            } catch (RuntimeException ex) {
                log.error(
                        "Payment is confirmed for order {}, but inventory could not be decreased "
                                + "for medicine {} (quantity {}). Order will still be marked paid; "
                                + "inventory requires follow-up.",
                        order.getId(),
                        item.getMedicineId(),
                        item.getQuantity(),
                        ex
                );
            }
        }

        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);

        System.out.println(
                "🎉 ORDER MARKED PAID: " + order.getId()
        );
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.failed}",
            groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.arvind.order.event.PaymentFailedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional
    public void consumePaymentFailed(PaymentFailedEvent event) {

        System.out.println(
                "❌ PAYMENT FAILED EVENT RECEIVED: orderId="
                        + event.getOrderId()
        );

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for payment.failed event: " + event.getOrderId()
                ));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            System.out.println(
                    "ℹ️ IGNORING PAYMENT FAILED EVENT FOR ORDER STATUS "
                            + order.getStatus() + ": " + order.getId()
            );
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        System.out.println(
                "🚫 ORDER CANCELLED: " + order.getId()
        );
    }
}
