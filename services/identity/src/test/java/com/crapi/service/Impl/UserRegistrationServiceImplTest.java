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

import com.crapi.config.JwtAuthTokenFilter;
import com.crapi.config.JwtProvider;
import com.crapi.constant.UserMessage;
import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.entity.VehicleDetails;
import com.crapi.enums.ERole;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.SignUpForm;
import com.crapi.repository.ChangeEmailRepository;
import com.crapi.repository.ProfileVideoRepository;
import com.crapi.repository.UserDetailsRepository;
import com.crapi.repository.UserRepository;
import com.crapi.service.VehicleService;
import com.crapi.utils.SMTPMailServer;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class UserRegistrationServiceImplTest {

  @InjectMocks @Spy private UserRegistrationServiceImpl userRegistrationService;
  @Mock private PasswordEncoder encoder;
  @Mock private UserRepository userRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtAuthTokenFilter jwtAuthTokenFilter;
  @Mock private JwtProvider jwtProvider;
  @Mock private UserDetailsRepository userDetailsRepository;
  @Mock private VehicleService vehicleService;
  @Mock private SMTPMailServer smtpMailServer;
  @Mock private ProfileVideoRepository profileVideoRepository;
  @Mock private ChangeEmailRepository changeEmailRepository;
  @Mock Appender appender;
  @Captor ArgumentCaptor<LogEvent> logCaptor;

  @Test
  public void registerUserReturnsNumberAlreadyExists() {
    SignUpForm signUpForm = getDummySignUpForm();
    String expectedMessage = UserMessage.NUMBER_ALREADY_REGISTERED + signUpForm.getNumber();
    Mockito.when(userRepository.existsByNumber(signUpForm.getNumber())).thenReturn(true);
    Assertions.assertEquals(
        userRegistrationService.registerUser(signUpForm).getMessage(), expectedMessage);
    Mockito.verify(userRepository, Mockito.times(1)).existsByNumber(Mockito.any());
    Mockito.verify(userRepository, Mockito.times(0)).existsByEmail(Mockito.any());
  }

  @Test
  public void registerUserReturnsEmailAlreadyExists() {
    SignUpForm signUpForm = getDummySignUpForm();
    String expectedMessage = UserMessage.EMAIL_ALREADY_REGISTERED + signUpForm.getEmail();
    Mockito.when(userRepository.existsByNumber(signUpForm.getNumber())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(signUpForm.getEmail())).thenReturn(true);
    Assertions.assertEquals(
        userRegistrationService.registerUser(signUpForm).getMessage(), expectedMessage);
    Mockito.verify(userRepository, Mockito.times(1)).existsByNumber(Mockito.any());
    Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(Mockito.any());
  }

  @Test(expected = EntityNotFoundException.class)
  public void registerUserFailsNotAbleToCreateVehicleDetails() {
    SignUpForm signUpForm = getDummySignUpForm();
    User user = getDummyUser();
    Mockito.when(userRepository.existsByNumber(signUpForm.getNumber())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(signUpForm.getEmail())).thenReturn(false);
    Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("EncodedString");
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
    Mockito.when(userDetailsRepository.save(Mockito.any(UserDetails.class)))
        .thenReturn(getDummyUserDetails());
    Mockito.when(vehicleService.createVehicle()).thenReturn(null);
    userRegistrationService.registerUser(signUpForm);
  }

  @Test
  public void registerUserSuccessFull() {
    SignUpForm signUpForm = getDummySignUpForm();
    User user = getDummyUser();
    Mockito.when(userRepository.existsByNumber(signUpForm.getNumber())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(signUpForm.getEmail())).thenReturn(false);
    Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("EncodedString");
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
    Mockito.when(userDetailsRepository.save(Mockito.any(UserDetails.class)))
        .thenReturn(getDummyUserDetails());
    Mockito.when(vehicleService.createVehicle()).thenReturn(new VehicleDetails());
    Mockito.doNothing()
        .when(smtpMailServer)
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    Assertions.assertEquals(
        userRegistrationService.registerUser(signUpForm).getStatus(), HttpStatus.OK.value());
    Mockito.verify(smtpMailServer, Mockito.times(1))
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
  }

  private SignUpForm getDummySignUpForm() {
    SignUpForm signUpForm = new SignUpForm(1l, "Name", "email@example.com", "9999999");
    signUpForm.setPassword("myPass");
    return signUpForm;
  }

  private UserDetails getDummyUserDetails() {
    UserDetails userDetails = new UserDetails();
    userDetails.setUser(getDummyUser());
    userDetails.setAvailable_credit(200.89);
    userDetails.setName("User1 Details");
    userDetails.setPicture(new byte[] {0, 1, 0});
    return userDetails;
  }

  private User getDummyUser() {
    User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
    user.setId(1l);
    return user;
  }
}
