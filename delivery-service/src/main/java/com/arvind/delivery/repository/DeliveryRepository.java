package com.arvind.delivery.repository;

import com.arvind.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    Optional<Delivery> findByIdAndUserId(Long id, String userId);

    Optional<Delivery> findByOrderIdAndUserId(Long orderId, String userId);

    List<Delivery> findByUserId(String userId);
}