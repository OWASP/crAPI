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

package com.crapi.config;

import com.crapi.constant.TestUsers;
import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.entity.VehicleDetails;
import com.crapi.enums.ERole;
import com.crapi.model.SeedUser;
import com.crapi.repository.*;
import com.crapi.service.VehicleService;
import com.crapi.utils.UserData;
import com.crapi.utils.VehicleLocationData;
import com.crapi.utils.VehicleModelData;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class InitialDataConfig {

  private static final Logger logger = LoggerFactory.getLogger(InitialDataConfig.class);

  @Autowired VehicleLocationRepository vehicleLocationRepository;

  @Autowired VehicleModelRepository vehicleModelRepository;

  @Autowired VehicleDetailsRepository vehicleDetailsRepository;

  @Autowired UserDetailsRepository userDetailsRepository;

  @Autowired UserRepository userRepository;

  @Autowired ProfileVideoRepository profileVideoRepository;

  @Autowired VehicleService vehicleService;

  @Autowired PasswordEncoder encoder;

  public void addLocation() {
    if (CollectionUtils.isEmpty(vehicleLocationRepository.findAll())) {
      VehicleLocationData vehicleLocationData = new VehicleLocationData();
      vehicleLocationRepository.saveAll(vehicleLocationData.getVehicleLocationData());
    }
  }

  public void addVehicleModel() {
    if (CollectionUtils.isEmpty(vehicleModelRepository.findAll())) {
      VehicleModelData vehicleModelData = new VehicleModelData();
      vehicleModelRepository.saveAll(vehicleModelData.getModelList());
    }
  }

  @EventListener
  public void setup(ApplicationReadyEvent event) {

    addLocation();
    addVehicleModel();
    addUser();
  }

  public void addUser() {
    if (CollectionUtils.isEmpty(userDetailsRepository.findAll())) {
      ArrayList<SeedUser> userDetailList = new TestUsers().getUsers();
      for (SeedUser userDetails : userDetailList) {
        boolean user =
            predefineUserData(
                userDetails.getName(),
                userDetails.getEmail(),
                userDetails.getPassword(),
                userDetails.getNumber(),
                userDetails.getRole());
        if (!user) {
          logger.error("Fail to create predefined users");
        }
      }
    }
  }

  public boolean predefineUserData(
      String name, String email, String password, String number, ERole role) {
    UserData userData = new UserData();
    VehicleDetails vehicleDetails = null;
    UserDetails userDetails = null;
    try {
      User user = new User(email, number, encoder.encode(password), role);
      user = userRepository.save(user);
      userDetails = userData.getPredefineUser(name, user);
      userDetailsRepository.save(userDetails);
      vehicleDetails = vehicleService.createVehicle();
      if (vehicleDetails != null) {
        vehicleDetails.setOwner(user);
        vehicleDetailsRepository.save(vehicleDetails);
        return true;
      }
      logger.error("Fail to create vehicle for user {}", email);
      return false;
    } catch (Exception e) {
      logger.error("Fail to create user {}, Exception :: {}", email, e);
      return false;
    }
  }
}
