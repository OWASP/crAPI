package com.crapi.model;

import lombok.Data;

@Data
public class VehicleOwnership {
  private String vin;
  private String rank;
  private String name;
  private String phone;
  private String email;
  private String ssn;
  private String address;
  // map to registration_id from json
  private String registration_id;
  // map to registration_date from json
  private String registration_date;

  public VehicleOwnership() {}
}
