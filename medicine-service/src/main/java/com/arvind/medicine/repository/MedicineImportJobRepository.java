package com.arvind.medicine.repository;

import com.arvind.medicine.entity.MedicineImportJob;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MedicineImportJobRepository extends JpaRepository<MedicineImportJob, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select job from MedicineImportJob job where job.id = :id")
    Optional<MedicineImportJob> findByIdForUpdate(@Param("id") String id);
}
