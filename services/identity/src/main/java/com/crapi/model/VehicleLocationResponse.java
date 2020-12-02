package com.crapi.model;

import com.crapi.entity.VehicleLocation;
import lombok.Data;

import java.util.UUID;

/**
 * @author Traceabel AI
 */

@Data
public class VehicleLocationResponse {

    private UUID carId;
    private VehicleLocation vehicleLocation;
    private String fullName;

    public VehicleLocationResponse(){

    }

    public VehicleLocationResponse(UUID id, String name, VehicleLocation vehicleLocation){
        this.carId =id;
        this.fullName = name;
        this.vehicleLocation = vehicleLocation;

    }
}
