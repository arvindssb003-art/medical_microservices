package com.arvind.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryResponse {

    private Long id;
    private Long medicineId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer reorderLevel;
}
