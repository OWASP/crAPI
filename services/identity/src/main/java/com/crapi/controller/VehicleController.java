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

package com.crapi.controller;

import com.crapi.constant.UserMessage;
import com.crapi.entity.VehicleDetails;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VehicleForm;
import com.crapi.model.VehicleLocationResponse;
import com.crapi.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * @author Traceable AI
 */

@CrossOrigin
@RestController
@RequestMapping("/identity/api/v2")
public class VehicleController {

    @Autowired
    VehicleService vehicleService;


    /**
     * @param vehicleDetails
     * @return response of success and failure message
     * save vehicle Details for user in database
     */
    @PostMapping("/vehicle/add_vehicle")
    public ResponseEntity<CRAPIResponse> addVehicle(@Valid @RequestBody VehicleForm vehicleDetails, HttpServletRequest request){
        CRAPIResponse checkVehicleResponse =vehicleService.checkVehicle(vehicleDetails, request);
        if(checkVehicleResponse!=null && checkVehicleResponse.getStatus()==200){
            return ResponseEntity.status(HttpStatus.OK).body(checkVehicleResponse);
        }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(checkVehicleResponse);

    }

    /**
     * @param request
     * @return send vehicle details to user by email address
     */
    @PostMapping("/vehicle/resend_email")
    public ResponseEntity<CRAPIResponse> getVehicleDetails(HttpServletRequest request){
        CRAPIResponse vehicleResponse = vehicleService.sendVehicleDetails(request);
        if (vehicleResponse!=null && vehicleResponse.getStatus()==200) {
            return ResponseEntity.status(HttpStatus.OK).body(vehicleResponse);
        }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CRAPIResponse
                    (UserMessage.INTERNAL_SERVER_ERROR,500));
    }

    /**
     * @param request
     * @return this api returns List of vehicle of user
     * Dashboard Vehicle details fetch by this api
     */
    @GetMapping("/vehicle/vehicles")
    public ResponseEntity<?> getVehicle(HttpServletRequest request){
        List<VehicleDetails> vehicleDetails = vehicleService.getVehicleDetails(request);
        if (vehicleDetails!=null) {
            return ResponseEntity.status(HttpStatus.OK).body(vehicleDetails);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CRAPIResponse
                    (UserMessage.DID_NOT_GET_VEHICLE_FOR_USER,500));
    }



    /**
     * @param carId
     * @return VehicleDetails on given car_id.
     */
    @GetMapping("/vehicle/{carId}/location")
    public ResponseEntity<?> getLocationBOLA(@PathVariable("carId")UUID carId){
        VehicleLocationResponse vehicleDetails = vehicleService.getVehicleLocation(carId);
        if (vehicleDetails!=null)
            return ResponseEntity.ok().body(vehicleDetails);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).
                    body( new CRAPIResponse(UserMessage.DID_NOT_GET_VEHICLE_FOR_USER));
    }
}
