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

package com.crapi.repository;

import com.crapi.entity.VehicleDetails;
import com.crapi.model.VehicleForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Traceable AI
 */
@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Long> {
    VehicleDetails findByVin(String vin);

    VehicleDetails findByUuid(UUID uuid);

    List<VehicleDetails> findAllByOwner_id(Long id);

    VehicleDetails findByVehicleLocation_id(Long carId);

    VehicleDetails findByOwner_id(Long id);
}
