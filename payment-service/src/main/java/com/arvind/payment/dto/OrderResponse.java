package com.arvind.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
}