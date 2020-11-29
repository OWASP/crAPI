package com.crapi.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Traceabel AI
 */

@Entity
@Table(name = "vehicle_company")
@Data
public class VehicleCompany implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;

    public VehicleCompany(){}

    public VehicleCompany(String name){
        this.name = name;
    }

}
