package com.arvind.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Long id;
    private String userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}