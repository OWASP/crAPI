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

package com.crapi.model;

import com.crapi.entity.VehicleLocation;
import java.util.UUID;
import lombok.Data;

@Data
public class VehicleLocationResponse {

  private UUID carId;
  private VehicleLocation vehicleLocation;
  private String fullName;
  private String email;

  public VehicleLocationResponse() {}

  public VehicleLocationResponse(
      UUID id, String name, String email, VehicleLocation vehicleLocation) {
    this.carId = id;
    this.fullName = name;
    this.email = email;
    this.vehicleLocation = vehicleLocation;
  }
}
