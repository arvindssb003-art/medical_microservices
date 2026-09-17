package com.arvind.cart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicineResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private boolean prescriptionRequired;
    private boolean active;
}
