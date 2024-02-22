/*
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

import com.crapi.enums.EFuelType;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

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

  @ManyToOne(cascade = CascadeType.ALL)
  private VehicleCompany vehiclecompany;

  public VehicleModel() {}

  public VehicleModel(
      String model, EFuelType fuelType, VehicleCompany vehicleCompany, String vehicle_img) {
    this.model = model;
    this.fuel_type = fuelType;
    this.vehiclecompany = vehicleCompany;
    this.vehicle_img = vehicle_img;
  }
}
