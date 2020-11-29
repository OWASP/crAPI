package com.crapi.repository;

import com.crapi.entity.VehicleCompany;
import com.crapi.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Traceabel AI
 */
@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel,Long> {

    List<VehicleModel> findByVehiclecompany_id(long id);
}
