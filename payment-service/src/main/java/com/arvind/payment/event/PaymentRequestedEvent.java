package com.arvind.payment.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestedEvent {

    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}