package com.arvind.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long medicineId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}