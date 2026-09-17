package com.arvind.cart.repository;

import com.arvind.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndMedicineId(Long cartId, Long medicineId);
}