package com.crapi.entity;

import com.crapi.enums.EFuelType;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Traceabel AI
 */

@Entity
@Table(name = "vehicle_model")
@Data
public class VehicleModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String model;
    private EFuelType fuel_type;
    private String vehicle_img;

    @OneToOne(cascade = {CascadeType.ALL})
    private VehicleCompany vehiclecompany;

    public VehicleModel(){}

    public VehicleModel(String model, EFuelType fuelType, VehicleCompany vehicleCompany, String vehicle_img){
        this.model = model;
        this.fuel_type=fuelType;
        this.vehiclecompany = vehicleCompany;
        this.vehicle_img = vehicle_img;
    }
}
