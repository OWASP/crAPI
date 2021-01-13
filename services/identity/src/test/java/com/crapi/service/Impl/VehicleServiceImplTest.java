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

package com.crapi.service.Impl;

import com.google.api.Http;
import com.crapi.constant.UserMessage;
import com.crapi.entity.*;
import com.crapi.enums.EFuelType;
import com.crapi.enums.ERole;
import com.crapi.enums.EStatus;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VehicleForm;
import com.crapi.model.VehicleLocationResponse;
import com.crapi.repository.UserDetailsRepository;
import com.crapi.repository.VehicleDetailsRepository;
import com.crapi.repository.VehicleLocationRepository;
import com.crapi.repository.VehicleModelRepository;
import com.crapi.service.UserService;
import com.crapi.utils.SMTPMailServer;
import com.crapi.exception.CRAPIExceptionHandler;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class VehicleServiceImplTest {

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Mock
    private VehicleModelRepository vehicleModelRepository;
    @Mock
    private VehicleLocationRepository vehicleLocationRepository;
    @Mock
    private VehicleDetailsRepository vehicleDetailsRepository;
    @Mock
    private UserDetailsRepository userDetailsRepository;
    @Mock
    private UserService userService;
    @Mock
    private SMTPMailServer smtpMailServer;

    @Test
    public void addVehicleDetailsSuccess() {
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        User user = getDummyUser();
        vehicleDetails.setPincode(vehicleForm.getPincode());
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        Assertions.assertTrue(vehicleService.addVehicleDetails(vehicleForm, getDummyHttpRequest()));
        Mockito.verify(vehicleDetailsRepository, Mockito.times(1)).save(Mockito.any());

    }

    @Test
    public void addVehicleDetailsFailWhenVehicleDetailsNotPresent() {
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        vehicleDetails.setPincode(vehicleForm.getPincode());
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(null);
        Assertions.assertFalse(vehicleService.addVehicleDetails(vehicleForm, getDummyHttpRequest()));
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());

    }

    @Test
    public void addVehicleDetailsFailWhenVehicleFormPinCodeNotEqualVehicleDetailPinCode() {
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Assertions.assertFalse(vehicleService.addVehicleDetails(vehicleForm, getDummyHttpRequest()));
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void addVehicleFailWhenUserNotPresent() {
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        vehicleDetails.setPincode(vehicleForm.getPincode());
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(null);
        Assertions.assertFalse(vehicleService.addVehicleDetails(vehicleForm, getDummyHttpRequest()));
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void getVehicleDetailsSuccessFull() {
        User user = getDummyUser();
        List<VehicleDetails> vehicleDetailsList = Collections.singletonList(getDummyVehicleDetails());
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        Mockito.when(vehicleDetailsRepository.findAllByOwner_id(Mockito.anyLong()))
                .thenReturn(vehicleDetailsList);
        List<VehicleDetails> actualVehicleDetailsList = vehicleService.getVehicleDetails(getDummyHttpRequest());
        assertEquals(actualVehicleDetailsList.size(), 1);
        for (VehicleDetails vehicleDetails : actualVehicleDetailsList) {
            Assertions.assertNull(vehicleDetails.getOwner());
        }
    }

    @Test (expected = CRAPIExceptionHandler.class)
    public void getVehicleDetailsReturnWhenUserNotFound() {
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(null);
        vehicleService.getVehicleDetails(getDummyHttpRequest());
    }

    @Test
    public void getVehicleDetailsReturnWhenNoVehicleFound() {
        User user = getDummyUser();
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        Mockito.when(vehicleDetailsRepository.findAllByOwner_id(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());
        Assertions.assertTrue(vehicleService.getVehicleDetails(getDummyHttpRequest()).isEmpty());
    }

    @Test
    public void getVehicleLocationSuccessWithUserDetailsNotNull() {
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        UserDetails userDetails = getDummyUserDetails();
        Mockito.when(vehicleDetailsRepository.findByUuid(vehicleDetails.getUuid()))
                .thenReturn(vehicleDetails);
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(userDetails);
        VehicleLocationResponse vehicleLocationResponse = vehicleService.getVehicleLocation(vehicleDetails.getUuid());
        Assertions.assertNotNull(vehicleLocationResponse);
        Assertions.assertEquals(userDetails.getName(), vehicleLocationResponse.getFullName());
    }

    @Test
    public void getVehicleLocationSuccessWithUserDetailsNull() {
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        UserDetails userDetails = getDummyUserDetails();
        userDetails.setName(null);
        Mockito.when(vehicleDetailsRepository.findByUuid(vehicleDetails.getUuid()))
                .thenReturn(vehicleDetails);
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(userDetails);
        VehicleLocationResponse vehicleLocationResponse = vehicleService.getVehicleLocation(vehicleDetails.getUuid());
        Assertions.assertNotNull(vehicleLocationResponse);
        Assertions.assertNull(vehicleLocationResponse.getFullName());
    }

    @Test
    public void getVehicleLocationNotFoundWhenVehicleDetailsAreNull() {
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        Mockito.when(vehicleDetailsRepository.findByUuid(vehicleDetails.getUuid()))
                .thenReturn(null);
        VehicleLocationResponse vehicleLocationResponse = vehicleService.getVehicleLocation(vehicleDetails.getUuid());
        Assertions.assertNull(vehicleLocationResponse);
    }

    @Test
    public void getVehicleLocationNotFoundWhenVehicleDetailsOwnerIsNull() {
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        vehicleDetails.setOwner(null);
        Mockito.when(vehicleDetailsRepository.findByUuid(vehicleDetails.getUuid()))
                .thenReturn(vehicleDetails);
        VehicleLocationResponse vehicleLocationResponse = vehicleService.getVehicleLocation(vehicleDetails.getUuid());
        Assertions.assertNull(vehicleLocationResponse);
    }

    @Test
    public void checkVehicleSuccessFull(){
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        User user = getDummyUser();
        vehicleDetails.setOwner(null);
        vehicleDetails.setPincode(vehicleForm.getPincode());
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        CRAPIResponse crapiAPIResponse = vehicleService.checkVehicle(vehicleForm,getDummyHttpRequest());
        Mockito.verify(vehicleDetailsRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertEquals(UserMessage.VEHICLE_SAVED_SUCCESSFULLY, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void checkVehicleFailWhenPinCodeNotEqual(){
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        User user = getDummyUser();
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        CRAPIResponse crapiAPIResponse = vehicleService.checkVehicle(vehicleForm,getDummyHttpRequest());
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());
        Assertions.assertEquals(UserMessage.VEHICLE_ALREADY_CREATED, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void checkVehicleFailWhenVehicleDetailsNotFound(){
        VehicleForm vehicleForm = getDummyVehicleForm();
        User user = getDummyUser();
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        CRAPIResponse crapiAPIResponse = vehicleService.checkVehicle(vehicleForm,getDummyHttpRequest());
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());
        Assertions.assertEquals(UserMessage.VEHICLE_ALREADY_CREATED, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void checkVehicleFailWhenUserNotFound(){
        VehicleForm vehicleForm = getDummyVehicleForm();
        VehicleDetails vehicleDetails = getDummyVehicleDetails();
        vehicleDetails.setPincode(vehicleForm.getPincode());
        Mockito.when(vehicleDetailsRepository.findByVin(Mockito.anyString()))
                .thenReturn(vehicleDetails);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(null);
        CRAPIResponse crapiAPIResponse = vehicleService.checkVehicle(vehicleForm,getDummyHttpRequest());
        Mockito.verify(vehicleDetailsRepository, Mockito.times(0)).save(Mockito.any());
        Assertions.assertEquals(UserMessage.VEHICLE_ALREADY_CREATED, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void sendVehicleDetailsSuccessFullWithOwnerNull(){
        User user = getDummyUser();
        UserDetails userDetails = getDummyUserDetails();
        VehicleDetails vehicleDetails1 = getDummyVehicleDetails();
        VehicleDetails vehicleDetails2 = getDummyVehicleDetails();
        vehicleDetails2.setOwner(null);
        List<VehicleDetails> vehicleDetailsList = Arrays.asList(vehicleDetails1,vehicleDetails2);
        Mockito.when(userService.getUserFromToken(Mockito.any()))
                .thenReturn(user);
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(userDetails);
       Mockito.when(vehicleDetailsRepository.findAll())
               .thenReturn(vehicleDetailsList);
       Mockito.doNothing().when(smtpMailServer).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
       CRAPIResponse crapiAPIResponse = vehicleService.sendVehicleDetails(getDummyHttpRequest());
       Assertions.assertEquals(UserMessage.VEHICLE_DETAILS_SENT_TO_EMAIL, crapiAPIResponse.getMessage());
       Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());
    }


    private UserDetails getDummyUserDetails() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUser(getDummyUser());
        userDetails.setAvailable_credit(200.89);
        userDetails.setName("User1 Details");
        userDetails.setPicture(new byte[]{0, 1, 0});
        return userDetails;
    }


    private VehicleDetails getDummyVehicleDetails() {
        VehicleDetails vehicleDetails = new VehicleDetails();
        vehicleDetails.setId(1l);
        vehicleDetails.setOwner(getDummyUser());
        vehicleDetails.setPincode("981281");
        vehicleDetails.setStatus(EStatus.ACTIVE);
        vehicleDetails.setUuid(UUID.randomUUID());
        vehicleDetails.setVehicleLocation(getDummyVehicleLocation());
        vehicleDetails.setVin("dummyVin");
        vehicleDetails.setYear(2020);
        vehicleDetails.setModel(getDummyVehicleModel());
        return vehicleDetails;
    }

    private VehicleModel getDummyVehicleModel() {
        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setFuel_type(EFuelType.CNG);
        vehicleModel.setId(1l);
        vehicleModel.setModel("Ducati");
        vehicleModel.setVehicle_img("dummy image");
        vehicleModel.setVehiclecompany(getDummyVehicleCompany());
        return vehicleModel;
    }

    private VehicleCompany getDummyVehicleCompany() {
        VehicleCompany vehicleCompany = new VehicleCompany();
        vehicleCompany.setId(1l);
        vehicleCompany.setName("DummyCompany");
        return vehicleCompany;
    }

    private VehicleLocation getDummyVehicleLocation() {
        VehicleLocation vehicleLocation = new VehicleLocation();
        vehicleLocation.setId(1l);
        vehicleLocation.setLatitude("12.8979801");
        vehicleLocation.setLongitude("77.8709012");
        return vehicleLocation;
    }

    private User getDummyUser() {
        User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
        user.setId(1l);
        return user;
    }


    private VehicleForm getDummyVehicleForm() {
        VehicleForm vehicleForm = new VehicleForm();
        vehicleForm.setPincode("281921");
        vehicleForm.setVin("dummyVin");
        return vehicleForm;
    }

    private MockHttpServletRequest getDummyHttpRequest() {
        return new MockHttpServletRequest();
    }
}