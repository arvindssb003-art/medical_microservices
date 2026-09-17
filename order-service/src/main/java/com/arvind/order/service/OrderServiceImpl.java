package com.arvind.order.service;

import com.arvind.order.client.CartClient;
import com.arvind.order.client.InventoryClient;
import com.arvind.order.client.MedicineClient;
import com.arvind.order.client.UserClient;
import com.arvind.order.dto.CartResponse;
import com.arvind.order.dto.CreateOrderRequest;
import com.arvind.order.dto.InventoryResponse;
import com.arvind.order.dto.MedicineResponse;
import com.arvind.order.dto.OrderItemRequest;
import com.arvind.order.dto.OrderItemResponse;
import com.arvind.order.dto.OrderResponse;
import com.arvind.order.dto.UserResponse;
import com.arvind.order.entity.Order;
import com.arvind.order.entity.OrderItem;
import com.arvind.order.exception.InvalidOrderException;
import com.arvind.order.exception.OrderNotFoundException;
import com.arvind.order.repository.OrderItemRepository;
import com.arvind.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final MedicineClient medicineClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public OrderResponse createOrder(
            String userId,
            CreateOrderRequest request) {

        UserResponse user = userClient.getUser(userId);

        if (user == null || !user.isEnabled()) {
            throw new InvalidOrderException(
                    "User is not available"
            );
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException(
                    "Order must contain at least one item"
            );
        }

        Order order = Order.builder()
                .userId(userId)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            MedicineResponse medicine =
                    medicineClient.getMedicine(
                            itemRequest.getMedicineId()
                    );

            if (medicine == null || !medicine.isActive()) {
                throw new InvalidOrderException(
                        "Medicine is not available: "
                                + itemRequest.getMedicineId()
                );
            }

            InventoryResponse inventory =
                    inventoryClient.getInventory(
                            itemRequest.getMedicineId()
                    );

            if (inventory == null
                    || inventory.getAvailableQuantity()
                    < itemRequest.getQuantity()) {

                throw new InvalidOrderException(
                        "Insufficient stock for medicine: "
                                + itemRequest.getMedicineId()
                );
            }

            BigDecimal subtotal =
                    medicine.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            );

            OrderItem orderItem = OrderItem.builder()
                    .medicineId(medicine.getId())
                    .medicineName(medicine.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(medicine.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(
            String userId,
            Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {

        return orderRepository
                .findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(
            String userId,
            Long orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        order.setStatus(
                com.arvind.order.entity.OrderStatus.CANCELLED
        );

        orderRepository.save(order);
    }

    private OrderResponse buildOrderResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMedicineId(),
                        item.getMedicineName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getOrderDate(),
                items
        );
    }
}