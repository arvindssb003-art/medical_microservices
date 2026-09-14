package com.arvind.medicine.service;

import com.arvind.medicine.dto.MedicineRequest;
import com.arvind.medicine.dto.MedicineResponse;
import com.arvind.medicine.entity.MedicineCategory;

import java.util.List;

public interface MedicineService {

    MedicineResponse createMedicine(MedicineRequest request);

    MedicineResponse getMedicineById(Long id);

    List<MedicineResponse> getAllMedicines();

    List<MedicineResponse> getActiveMedicines();

    List<MedicineResponse> getMedicinesByCategory(MedicineCategory category);

    List<MedicineResponse> searchMedicines(String name);

    MedicineResponse updateMedicine(Long id, MedicineRequest request);

    void deleteMedicine(Long id);
}