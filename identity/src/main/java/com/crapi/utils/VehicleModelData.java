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
