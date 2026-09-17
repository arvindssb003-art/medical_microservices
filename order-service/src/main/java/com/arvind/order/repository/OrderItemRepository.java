package com.arvind.order.repository;

import com.arvind.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    Optional<OrderItem> findByOrderIdAndMedicineId(
            Long orderId,
            Long medicineId
    );
}