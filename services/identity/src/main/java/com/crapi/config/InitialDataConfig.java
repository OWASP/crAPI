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

package com.crapi.config;


import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.entity.VehicleDetails;
import com.crapi.enums.ERole;
import com.crapi.repository.*;
import com.crapi.service.Impl.VehicleServiceImpl;
import com.crapi.service.VehicleService;
import com.crapi.utils.UserData;
import com.crapi.utils.VehicleLocationData;
import com.crapi.utils.VehicleModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Traceable AI
 */
@Component
public class InitialDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(InitialDataConfig.class);

    @Autowired
    VehicleLocationRepository vehicleLocationRepository;

    @Autowired
    VehicleModelRepository vehicleModelRepository;

    @Autowired
    VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileVideoRepository profileVideoRepository;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    PasswordEncoder encoder;


    public void addLocation(){
        if (CollectionUtils.isEmpty(vehicleLocationRepository.findAll())) {
            VehicleLocationData vehicleLocationData = new VehicleLocationData();
            vehicleLocationRepository.saveAll(vehicleLocationData.getVehicleLocationData());
        }
    }

    public void addVehicleModel(){
        if (CollectionUtils.isEmpty(vehicleModelRepository.findAll())){
            VehicleModelData vehicleModelData = new VehicleModelData();
            vehicleModelRepository.saveAll(vehicleModelData.getModelList());
        }
    }

    @EventListener
    public void setup(ApplicationReadyEvent event){

        addLocation();
        addVehicleModel();
        addUser();
    }

    public void addUser(){
        if (CollectionUtils.isEmpty(userDetailsRepository.findAll())) {
            boolean user1 = predefineUserData("Adam","adam007@example.com","9876895423");
            boolean user2 = predefineUserData("Pogba", "pogba006@example.com", "9876570006");
            boolean user3 = predefineUserData("Robot", "robot001@example.com", "9876570001");
            if(!user1 || !user2 || !user1){
                logger.error("Fail to create user predefine data -> Message: {}");
            }
        }
    }

    public boolean predefineUserData(String name, String email, String number){
        UserData userData = new UserData();
        VehicleDetails vehicleDetails = null;
        UserDetails userDetails = null;
        try {

            User loginForm = new User(email, number, encoder.encode(name), ERole.ROLE_PREDEFINE);
            loginForm = userRepository.save(loginForm);
            userDetails = userData.getPredefineUser(name, loginForm);
            userDetailsRepository.save(userDetails);
            vehicleDetails = vehicleService.createVehicle();
            if (vehicleDetails != null) {
                vehicleDetails.setOwner(loginForm);
                vehicleDetailsRepository.save(vehicleDetails);
                return true;
            }
            logger.error("Fail to create vehicle for user {}", email);
            return false;
        } catch (Exception e){
            logger.error("Fail to create user {}, Exception :: {}", email, e);
            return false;
        }
    }

}
