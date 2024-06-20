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
import com.crapi.entity.ProfileVideo;
import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.entity.VehicleCompany;
import com.crapi.entity.VehicleDetails;
import com.crapi.entity.VehicleLocation;
import com.crapi.entity.VehicleModel;
import com.crapi.enums.EFuelType;
import com.crapi.enums.ERole;
import com.crapi.model.SeedUser;
import com.crapi.repository.*;
import com.crapi.service.VehicleService;
import com.crapi.utils.GenerateVIN;
import com.crapi.utils.UserData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class InitialDataConfig {

  private static long seed = 0;

  private Random random;

  GenerateVIN generateVIN;

  @Autowired VehicleLocationRepository vehicleLocationRepository;

  @Autowired VehicleCompanyRepository vehicleCompanyRepository;

  @Autowired VehicleModelRepository vehicleModelRepository;

  @Autowired VehicleDetailsRepository vehicleDetailsRepository;

  @Autowired UserDetailsRepository userDetailsRepository;

  @Autowired UserRepository userRepository;

  @Autowired ProfileVideoRepository profileVideoRepository;

  @Autowired VehicleService vehicleService;

  @Autowired PasswordEncoder encoder;

  public void createModels() {
    VehicleCompany vehicleCompany = new VehicleCompany("Hyundai");
    VehicleModel vehicleModel =
        new VehicleModel("Creta", EFuelType.DIESEL, vehicleCompany, "images/hyundai-creta.jpg");
    vehicleModelRepository.save(vehicleModel);

    vehicleCompany = new VehicleCompany("Lamborghini");
    vehicleModel =
        new VehicleModel(
            "Aventador", EFuelType.PETROL, vehicleCompany, "images/lamborghini-aventador.jpg");
    vehicleModel = vehicleModelRepository.save(vehicleModel);

    vehicleCompany = new VehicleCompany("Mercedes-Benz");
    vehicleModel =
        new VehicleModel(
            "GLA Class", EFuelType.PETROL, vehicleCompany, "images/mercedesbenz-gla.jpg");
    vehicleModelRepository.save(vehicleModel);

    vehicleCompany = new VehicleCompany("BMW");
    vehicleModel =
        new VehicleModel("5 Series", EFuelType.PETROL, vehicleCompany, "images/bmw-5.jpg");
    vehicleModelRepository.save(vehicleModel);

    vehicleCompany = new VehicleCompany("Audi");
    vehicleModel = new VehicleModel("RS7", EFuelType.PETROL, vehicleCompany, "images/audi-rs7.jpg");
    vehicleModelRepository.save(vehicleModel);

    vehicleCompany = new VehicleCompany("MG Motor");
    vehicleModel =
        new VehicleModel(
            "Hector Plus", EFuelType.PETROL, vehicleCompany, "images/mgmotor-hectorplus.jpg");
    vehicleModel = vehicleModelRepository.save(vehicleModel);
  }

  public void addVehicleModel() {
    if (CollectionUtils.isEmpty(vehicleModelRepository.findAll())) {
      createModels();
    }
  }

  @EventListener
  public void setup(ApplicationReadyEvent event) {
    random = new Random();
    random.setSeed(seed);
    generateVIN = new GenerateVIN();
    addVehicleModel();
    addUser();
  }

  public void addUser() {
    if (CollectionUtils.isEmpty(userDetailsRepository.findAll()) || false) {
      ArrayList<SeedUser> userDetailList = new TestUsers().getUsers();
      for (SeedUser userDetails : userDetailList) {
        boolean user =
            predefineUserData(
                userDetails.getName(),
                userDetails.getEmail(),
                userDetails.getPassword(),
                userDetails.getNumber(),
                userDetails.getRole(),
                userDetails.getCarid(),
                userDetails.getVin(),
                userDetails.getPincode(),
                userDetails.getLatitude(),
                userDetails.getLongitude());
        if (!user) {
          log.error("Fail to create predefined users");
        }
      }
    }
  }

  public VehicleDetails createVehicle(
      String carId, String vin, String pincode, String latitude, String longitude) {
    List<VehicleModel> modelList = null;
    modelList = vehicleModelRepository.findAll();
    if (modelList != null) {
      VehicleLocation vehicleLocation = new VehicleLocation(latitude, longitude);
      VehicleDetails vehicleDetails = new VehicleDetails(carId, pincode, vin);
      VehicleModel vehicleModel = modelList.get(random.nextInt(modelList.size()));
      vehicleModel = vehicleModelRepository.findById(vehicleModel.getId()).get();
      vehicleDetails.setVehicleLocation(vehicleLocation);
      vehicleDetails = vehicleDetailsRepository.save(vehicleDetails);
      vehicleDetails.setModel(vehicleModel);
      vehicleDetails = vehicleDetailsRepository.save(vehicleDetails);
      log.debug("Created vehicle for {} successfully", vehicleDetails);
      return vehicleDetails;
    }
    return null;
  }

  public boolean predefineUserData(
      String name,
      String email,
      String password,
      String number,
      ERole role,
      String carId,
      String vin,
      String pincode,
      String latitude,
      String longitude) {
    UserData userData = new UserData();
    VehicleDetails vehicleDetails = null;
    UserDetails userDetails = null;
    try {
      User user = new User(email, number, encoder.encode(password), role);
      user = userRepository.save(user);
      user = userRepository.findById(user.getId()).get();
      userDetails = userData.getPredefineUser(name, user);
      userDetails = userDetailsRepository.save(userDetails);
      vehicleDetails = createVehicle(carId, vin, pincode, latitude, longitude);
      if (vehicleDetails != null) {
        vehicleDetails.setOwner(user);
        vehicleDetailsRepository.save(vehicleDetails);
      } else {
        log.error("Fail to create vehicle for user {}", email);
        return false;
      }
      // generate random bytes
      byte[] videoBytes = new byte[10];
      random.nextBytes(videoBytes);
      String videoName = userDetails.getName().replace(" ", "_") + "_video";
      String conversionParam = "-v codec h264";
      ProfileVideo profileVideo = new ProfileVideo(videoName, videoBytes, user);
      profileVideo.setConversion_params(conversionParam);
      profileVideoRepository.save(profileVideo);
      return true;
    } catch (Exception e) {
      log.error("Fail to create user {}, Exception :: {}", email, e);
      return false;
    }
  }
}
