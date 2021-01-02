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

package com.crapi.utils;

import com.crapi.entity.VehicleCompany;
import com.crapi.entity.VehicleModel;
import com.crapi.enums.EFuelType;

import java.util.ArrayList;
import java.util.List;

public class VehicleModelData {


    /**
     * @return List of hard coded Vehicle Model and Company for pre data setup.
     */
    public List<VehicleModel> getModelList(){
        List<VehicleModel> vehicleModelList = new ArrayList<>();
        vehicleModelList.add(new VehicleModel("Creta", EFuelType.DIESEL,  new VehicleCompany("Hyundai"),"images/hyundai-creta.jpg"));
        vehicleModelList.add(new VehicleModel("Aventador", EFuelType.PETROL, new VehicleCompany("Lamborghini"),"images/lamborghini-aventador.jpg"));
        vehicleModelList.add(new VehicleModel("GLA Class", EFuelType.DIESEL, new VehicleCompany("Mercedes-Benz"),"images/mercedesbenz-gla.jpg"));
        vehicleModelList.add(new VehicleModel("5 Series", EFuelType.PETROL, new VehicleCompany("BMW"),"images/bmw-5.jpg"));
        vehicleModelList.add(new VehicleModel("RS7", EFuelType.DIESEL, new VehicleCompany("Audi"),"images/audi-rs7.jpg"));
        vehicleModelList.add(new VehicleModel("Hector Plus", EFuelType.PETROL, new VehicleCompany("MG Motor"),"images/mgmotor-hectorplus.jpg"));

        return vehicleModelList;
    }
}
