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

package com.crapi.service;

import com.crapi.entity.*;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VehicleForm;
import com.crapi.model.VehicleLocationResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author Traceable AI
 */

public interface VehicleService {

    boolean addVehicleDetails(VehicleForm vehicleDetails, HttpServletRequest request);

    VehicleDetails createVehicle();

    List<VehicleDetails> getVehicleDetails(HttpServletRequest request);

    VehicleLocationResponse getVehicleLocation(UUID carId);

    CRAPIResponse checkVehicle(VehicleForm vehicleDetails, HttpServletRequest request);

    CRAPIResponse sendVehicleDetails(HttpServletRequest request);
}
