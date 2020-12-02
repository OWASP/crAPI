package com.crapi.repository;

import com.crapi.entity.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author Traceabel AI
 */
@Repository
public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {
}
