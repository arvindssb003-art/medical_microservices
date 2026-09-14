package com.arvind.medical.inventory.repository;

import com.arvind.medical.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByMedicineId(Long medicineId);

    boolean existsByMedicineId(Long medicineId);
}