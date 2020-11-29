package com.crapi.service;

import com.crapi.entity.*;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VehicleForm;
import com.crapi.model.VehicleLocationResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author Traceabel AI
 */

public interface VehicleService {

    boolean addVehicleDetails(VehicleForm vehicleDetails, HttpServletRequest request);

    VehicleDetails createVehicle();

    List<VehicleDetails> getVehicleDetails(HttpServletRequest request);

    VehicleLocationResponse getVehicleLocation(UUID carId);

    CRAPIResponse checkVehicle(VehicleForm vehicleDetails, HttpServletRequest request);

    CRAPIResponse sendVehicleDetails(HttpServletRequest request);
}
