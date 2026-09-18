package com.arvind.payment.dto;

import com.arvind.payment.entity.PaymentMethod;
import com.arvind.payment.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}