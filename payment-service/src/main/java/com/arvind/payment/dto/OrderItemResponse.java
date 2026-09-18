package com.arvind.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}