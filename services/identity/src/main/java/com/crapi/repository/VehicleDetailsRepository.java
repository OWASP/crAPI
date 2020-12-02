package com.crapi.repository;

import com.crapi.entity.VehicleDetails;
import com.crapi.model.VehicleForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Traceabel AI
 */
@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Long> {
    VehicleDetails findByVin(String vin);

    VehicleDetails findByUuid(UUID uuid);

    List<VehicleDetails> findAllByOwner_id(Long id);

    VehicleDetails findByVehicleLocation_id(Long carId);

    VehicleDetails findByOwner_id(Long id);
}
