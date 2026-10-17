package com.arvind.payment.repository;

import com.arvind.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByIdAndUserId(Long id, String userId);

    Optional<Payment> findByOrderIdAndUserId(Long orderId, String userId);

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}