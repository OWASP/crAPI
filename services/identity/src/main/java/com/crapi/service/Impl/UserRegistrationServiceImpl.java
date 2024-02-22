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

package com.crapi.service.Impl;

import com.crapi.constant.UserMessage;
import com.crapi.entity.*;
import com.crapi.enums.ERole;
import com.crapi.enums.EStatus;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.*;
import com.crapi.repository.*;
import com.crapi.service.UserRegistrationService;
import com.crapi.service.VehicleService;
import com.crapi.utils.MailBody;
import com.crapi.utils.SMTPMailServer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
  private static final Logger logger = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);

  @Autowired UserRepository userRepository;

  @Autowired SMTPMailServer smtpMailServer;

  @Autowired UserDetailsRepository userDetailsRepository;

  @Autowired PasswordEncoder encoder;

  @Autowired VehicleService vehicleService;

  /**
   * @param name is signup user name which will set into user details
   * @param user Mapping user with user details
   * @return create user with default value and mapped with user.
   */
  public UserDetails createUserDetails(String name, User user) {
    UserDetails userDetails;
    try {
      userDetails = new UserDetails();
      userDetails.setName(name);
      userDetails.setUser(user);
      userDetails.setAvailable_credit(100.0);
      userDetails.setStatus(EStatus.ACTIVE.toString());
      return userDetails;
    } catch (Exception exception) {
      logger.error("fail to create UserDetails  Message: %d", exception);
    }
    return null;
  }

  /**
   * @param signUpRequest contains user email,number,name and password
   * @return boolean if user get saved in Database return true else false
   */
  @Transactional
  @Override
  public CRAPIResponse registerUser(SignUpForm signUpRequest) {
    User user;
    UserDetails userDetails;
    VehicleDetails vehicleDetails;
    // Check Number in database
    if (userRepository.existsByNumber(signUpRequest.getNumber())) {
      return new CRAPIResponse(
          UserMessage.NUMBER_ALREADY_REGISTERED + signUpRequest.getNumber(), 403);
    }
    // check Number in database
    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return new CRAPIResponse(
          UserMessage.EMAIL_ALREADY_REGISTERED + signUpRequest.getEmail(), 403);
    }
    // Register new user in Database
    user =
        new User(
            signUpRequest.getEmail(),
            signUpRequest.getNumber(),
            encoder.encode(signUpRequest.getPassword()),
            ERole.ROLE_USER);
    user = userRepository.save(user);
    if (user != null) {
      logger.info("User registered successful with userId {}", user.getId());
      // Creating User Details for same user
      userDetails = createUserDetails(signUpRequest.getName(), user);
      if (userDetails != null) {
        userDetailsRepository.save(userDetails);
        logger.info("User Details Created successful with userId {}", userDetails.getId());
      }

      // Creating User Vehicle
      vehicleDetails = vehicleService.createVehicle();
      if (vehicleDetails != null) {
        smtpMailServer.sendMail(
            user.getEmail(),
            MailBody.signupMailBody(
                vehicleDetails,
                (userDetails != null && userDetails.getName() != null
                    ? userDetails.getName()
                    : "")),
            "Welcome to crAPI");
        return new CRAPIResponse(UserMessage.SIGN_UP_SUCCESS_MESSAGE, 200);
      }
      throw new EntityNotFoundException(
          VehicleDetails.class, UserMessage.ERROR, signUpRequest.getName());
    }
    logger.info("User registration failed {}", signUpRequest.getEmail());
    return new CRAPIResponse(UserMessage.SIGN_UP_FAILED + signUpRequest.getEmail(), 400);
  }
}
