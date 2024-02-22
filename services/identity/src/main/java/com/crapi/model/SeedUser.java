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

import com.crapi.enums.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SeedUser {
  @NotBlank
  @Size(min = 3, max = 100)
  private String name;

  @NotBlank
  @Size(min = 6, max = 100)
  private String password;

  @NotBlank
  @Size(max = 100)
  @Email
  private String email;

  @NotBlank
  @Size(max = 15)
  private String number;

  @NotBlank
  @Size(min = 3, max = 100)
  private ERole role;

  @NotBlank
  @Size(min = 3, max = 100)
  private String carid;

  @NotBlank
  @Size(min = 3, max = 100)
  private String vin;

  @NotBlank
  @Size(min = 3, max = 100)
  private String pincode;

  @NotBlank
  @Size(min = 3, max = 100)
  private String latitude;

  @NotBlank
  @Size(min = 3, max = 100)
  private String longitude;

  public SeedUser(
      String name,
      String email,
      String number,
      String password,
      ERole role,
      String carid,
      String vin,
      String pincode,
      String latitude,
      String longitude) {
    this.name = name;
    this.email = email;
    this.number = number;
    this.password = password;
    this.role = role;
    this.carid = carid;
    this.vin = vin;
    this.pincode = pincode;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
