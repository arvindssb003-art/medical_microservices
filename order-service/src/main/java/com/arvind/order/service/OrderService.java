package com.arvind.order.service;

import com.arvind.order.dto.CreateOrderRequest;
import com.arvind.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(
            String userId,
            CreateOrderRequest request
    );

    OrderResponse getOrder(
            String userId,
            Long orderId
    );

    List<OrderResponse> getUserOrders(
            String userId
    );

    void cancelOrder(
            String userId,
            Long orderId
    );
}