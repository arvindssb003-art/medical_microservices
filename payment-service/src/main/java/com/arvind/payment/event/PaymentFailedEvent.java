package com.arvind.payment.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {

    private Long paymentId;
    private Long orderId;
    private String userId;
    private String reason;
}