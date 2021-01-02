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

import com.crapi.entity.VehicleLocation;

import java.util.ArrayList;
import java.util.List;


public class VehicleLocationData {

    /**
     * @return List of hard coded Vehicle Location for pre data setup
     */
    public List<VehicleLocation> getVehicleLocationData(){
        List<VehicleLocation> vehicleLocationData = new ArrayList<>();
        vehicleLocationData.add(new VehicleLocation("33.7967129","-84.3909149"));
        vehicleLocationData.add(new VehicleLocation("39.0247621","-77.1402267"));
        vehicleLocationData.add(new VehicleLocation("37.7775112","-122.3970889"));
        vehicleLocationData.add(new VehicleLocation("37.4850772","-122.1504711"));
        vehicleLocationData.add(new VehicleLocation("37.4171615","-122.0271935"));
        vehicleLocationData.add(new VehicleLocation("31.9726318","34.7958503"));
        vehicleLocationData.add(new VehicleLocation("37.233333","-115.808333"));
        vehicleLocationData.add(new VehicleLocation("28.6297622","77.2058573"));
        vehicleLocationData.add(new VehicleLocation("30.264007","-97.773161"));
        vehicleLocationData.add(new VehicleLocation("38.9518424","-77.1483682"));

        return  vehicleLocationData;
    }

}
