package com.arvind.order.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {

    private Long paymentId;
    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
}