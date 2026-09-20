package com.arvind.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryRequest(

        @NotNull
        Long orderId,

        @NotBlank
        String userId,

        @NotBlank
        String address
) {
}