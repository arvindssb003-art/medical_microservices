package com.arvind.medicine.service;

import com.arvind.medicine.dto.MedicineRequest;
import com.arvind.medicine.dto.MedicineResponse;
import com.arvind.medicine.entity.Medicine;
import com.arvind.medicine.entity.MedicineCategory;
import com.arvind.medicine.exception.MedicineNotFoundException;
import com.arvind.medicine.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import com.arvind.medicine.exception.MedicineAlreadyExistsException;

import java.util.List;

@Service
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public MedicineResponse createMedicine(MedicineRequest request) {

        if (medicineRepository.existsByNameIgnoreCase(request.getName())) {
            throw new MedicineAlreadyExistsException(request.getName());
        }

        Medicine medicine = new Medicine();

        mapRequestToEntity(request, medicine);

        Medicine savedMedicine = medicineRepository.save(medicine);

        return mapEntityToResponse(savedMedicine);
    }

    @Override
    public MedicineResponse getMedicineById(Long id) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));

        return mapEntityToResponse(medicine);
    }

    @Override
    public List<MedicineResponse> getAllMedicines() {

        return medicineRepository.findAll()
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    public List<MedicineResponse> getActiveMedicines() {

        return medicineRepository.findByActiveTrue()
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    public List<MedicineResponse> getMedicinesByCategory(
            MedicineCategory category) {

        return medicineRepository.findByCategory(category)
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    public List<MedicineResponse> searchMedicines(String name) {

        return medicineRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    public MedicineResponse updateMedicine(
            Long id,
            MedicineRequest request) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));

        mapRequestToEntity(request, medicine);

        Medicine updatedMedicine = medicineRepository.save(medicine);

        return mapEntityToResponse(updatedMedicine);
    }

    @Override
    public void deleteMedicine(Long id) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));

        medicineRepository.delete(medicine);
    }

    private void mapRequestToEntity(
            MedicineRequest request,
            Medicine medicine) {

        medicine.setName(request.getName());
        medicine.setGenericName(request.getGenericName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setCategory(request.getCategory());
        medicine.setDescription(request.getDescription());
        medicine.setPrice(request.getPrice());
        medicine.setPrescriptionRequired(
                request.isPrescriptionRequired()
        );
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setActive(request.isActive());
    }

    private MedicineResponse mapEntityToResponse(Medicine medicine) {

        MedicineResponse response = new MedicineResponse();

        response.setId(medicine.getId());
        response.setName(medicine.getName());
        response.setGenericName(medicine.getGenericName());
        response.setManufacturer(medicine.getManufacturer());
        response.setCategory(medicine.getCategory());
        response.setDescription(medicine.getDescription());
        response.setPrice(medicine.getPrice());
        response.setPrescriptionRequired(
                medicine.isPrescriptionRequired()
        );
        response.setExpiryDate(medicine.getExpiryDate());
        response.setActive(medicine.isActive());
        response.setCreatedAt(medicine.getCreatedAt());
        response.setUpdatedAt(medicine.getUpdatedAt());

        return response;
    }
}