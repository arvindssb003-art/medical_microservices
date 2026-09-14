package com.arvind.medicine.repository;

import com.arvind.medicine.entity.Medicine;
import com.arvind.medicine.entity.MedicineCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByCategory(MedicineCategory category);

    List<Medicine> findByActiveTrue();

    List<Medicine> findByNameContainingIgnoreCase(String name);

    List<Medicine> findByGenericNameContainingIgnoreCase(String genericName);

    boolean existsByNameIgnoreCase(String name);
}