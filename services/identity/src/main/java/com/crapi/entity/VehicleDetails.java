/*
 * Copyright 2020 Traceable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author Traceable AI
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

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="vehicle_model_id")
    private VehicleModel model;

    @OneToOne(cascade=CascadeType.ALL)
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
