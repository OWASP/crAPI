package com.crapi.entity;
import com.crapi.enums.EStatus;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.type.UUIDBinaryType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * @author Traceabel AI
 */

@Entity
@Table(name = "vehicle_details")
@Data
public class VehicleDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique=true)
    private UUID uuid = UUID.randomUUID();
    private String pincode;
    private String vin;
    private long year;
    private EStatus status;

    @OneToOne
    @JoinColumn(name="vehicle_model_id")
    private VehicleModel model;

    @OneToOne
    @JoinColumn(name = "location_id")
    private VehicleLocation vehicleLocation;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;

    public VehicleDetails(String pincode,String vin){

        this.pincode = pincode;
        this.vin = vin;
        this.status=EStatus.ACTIVE;
        this.year = LocalDate.now().getYear();

    }
    public VehicleDetails(){

    }

    //vehicle brand, model, year, VIN, pin code, owner_id


}
