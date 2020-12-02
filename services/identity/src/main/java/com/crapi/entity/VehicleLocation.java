package com.crapi.entity;

import lombok.Data;

import javax.persistence.*;
/**
 * @author Traceabel AI
 */

@Entity
@Table(name = "vehicle_location")
@Data
public class VehicleLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String latitude;
    private String longitude;

    public VehicleLocation(String latitude, String longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public VehicleLocation(){}

}
