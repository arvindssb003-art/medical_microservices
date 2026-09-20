package com.arvind.delivery.dto;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long orderId,
        String userId,
        String address,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}