package com.arvind.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}