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

import com.crapi.constant.UserMessage;
import com.crapi.entity.*;
import com.crapi.enums.EStatus;
import com.crapi.exception.CRAPIExceptionHandler;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VehicleForm;
import com.crapi.model.VehicleLocationResponse;
import com.crapi.repository.*;
import com.crapi.service.UserService;
import com.crapi.service.VehicleService;
import com.crapi.utils.GenerateVIN;
import com.crapi.utils.MailBody;
import com.crapi.utils.SMTPMailServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author Traceable AI
 */

@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);


    @Autowired
    VehicleModelRepository vehicleModelRepository;

    @Autowired
    VehicleLocationRepository vehicleLocationRepository;

    @Autowired
    VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    UserService userService;

    @Autowired
    SMTPMailServer smtpMailServer;


    /**
     * @param vehicleForm
     * @param request
     * @return VehicleDetails after saving into database
     */
    @Transactional
    @Override
    public boolean addVehicleDetails(VehicleForm vehicleForm, HttpServletRequest request) {
        try {
            VehicleDetails vehicleDetails = vehicleDetailsRepository.findByVin(vehicleForm.getVin());
            if (vehicleDetails!=null && vehicleDetails.getPincode().equalsIgnoreCase(vehicleForm.getPincode())) {
                User user = userService.getUserFromToken(request);
                    if (user != null) {
                        vehicleDetails.setOwner(user);
                        vehicleDetails.setStatus(EStatus.INACTIVE);
                        vehicleDetailsRepository.save(vehicleDetails);
                        logger.info("Success Owner updated in vehicle details");
                        return true;
                    }
                }
        } catch (Exception e) {
            logger.error("Fail to save vehicle details -> Message: {}", e);

        }
        return false;
    }

    /**
     * @return VehicleDetails when user get signup
     * this code will create random vehicle for user and sent email with vin and pincode
     * on register email
     */
    @Transactional
    @Override
    public VehicleDetails createVehicle() {
        String vin = "";
        String pincode ="";
        List<VehicleModel> modelList =null;
        GenerateVIN generateVIN = new GenerateVIN();
        VehicleLocation vehicleLocations = null;
        Random random = new Random();
            modelList = vehicleModelRepository.findAll();
            vehicleLocations = getVehicleLocationList();
            if (modelList!=null && vehicleLocations!=null){
                vin = generateVIN.generateVIN();
                pincode=generateVIN.generatePincode();
                VehicleDetails vehicleDetails = new VehicleDetails(pincode,vin);
                vehicleDetails.setModel(modelList.get(random.nextInt(modelList.size())));
                vehicleDetails.setVehicleLocation(vehicleLocations);
                vehicleDetailsRepository.save(vehicleDetails);
                return vehicleDetails;
            }
        throw new CRAPIExceptionHandler(UserMessage.CUSTOM_IO_EXCEPTION,
                    UserMessage.CUSTOM_IO_EXCEPTION_UNABLE_TO_CREATE_VEHICLE,500);
    }

    /**
     * @param request
     * @return list of vehicle of user
     */
    @Override
    public List<VehicleDetails> getVehicleDetails(HttpServletRequest request) {
        User user =null;
        List<VehicleDetails> vehicleDetailsList = null;
        try {
             user = userService.getUserFromToken(request);
            if (user != null) {
                vehicleDetailsList = vehicleDetailsRepository.findAllByOwner_id(user.getId());
                if (vehicleDetailsList!=null) {
                    for (VehicleDetails vehicleDetails: vehicleDetailsList){
                            vehicleDetails.setOwner(null);
                    }
                    return vehicleDetailsList;
                } else {
                    return Collections.emptyList();
                }
            }
        }catch (Exception exception){
            logger.error("Fail to get List of vehicle for user->{} -> Message: {}",user.getEmail(), exception);
        }
        throw new CRAPIExceptionHandler(UserMessage.CUSTOM_IO_EXCEPTION, UserMessage.VEHICLE_NOT_FOUND, 500);
    }

    /**
     * @param carId
     * @return VehicleDetails which is linked with this carId.
     */
    @Transactional
    @Override
    public VehicleLocationResponse getVehicleLocation(UUID carId) {
        VehicleDetails vehicleDetails = null;
        VehicleLocationResponse vehicleLocationForm = null;
        UserDetails userDetails = null;
        Random random = new Random();
        try {
            vehicleDetails = vehicleDetailsRepository.findByUuid(carId);
            if (vehicleDetails!=null) {
                // vehicleDetails = vehicleDetailsRepository.findByVehicleLocation_id(carId);
                //vehicleDetails.setVehicleLocation(getVehicleLocationList().get(random.nextInt(getVehicleLocationList().size())));
                if (vehicleDetails.getOwner()!=null) {
                    userDetails = userDetailsRepository.findByUser_id(vehicleDetails.getOwner().getId());
                    vehicleLocationForm = new VehicleLocationResponse(carId, 
                        (userDetails != null ? userDetails.getName() : null), vehicleDetails.getVehicleLocation());
                    return vehicleLocationForm;
                }
            }
        }catch (Exception exception){
            logger.error("Fail to get vehicle location-> Message: {}", exception);
        }
        return null;
    }

    /**
     * @param vehicleForm
     * @param request
     * @return Status, check given vehicle details for token user
     */
    @Transactional
    @Override
    public CRAPIResponse checkVehicle(VehicleForm vehicleForm, HttpServletRequest request) {
        VehicleDetails checkVehicle = null;
        User user = null;
            checkVehicle = vehicleDetailsRepository.findByVin(vehicleForm.getVin());
            user = userService.getUserFromToken(request);
            if (checkVehicle!=null && checkVehicle.getOwner()==null) {
                if (checkVehicle.getPincode().equalsIgnoreCase(vehicleForm.getPincode())) {
                    checkVehicle.setOwner(user);
                    checkVehicle.setStatus(EStatus.INACTIVE);
                    vehicleDetailsRepository.save(checkVehicle);
                    logger.info("Success Owner updated in vehicle details");
                    return new CRAPIResponse(UserMessage.VEHICLE_SAVED_SUCCESSFULLY, 200);
                }
            }
        return new CRAPIResponse(UserMessage.VEHICLE_ALREADY_CREATED, 403);
    }

    /**
     * @param request
     * @return status of send mail of vehicle details.
     * This function send mail of vehicle details
     * checks if  vehicle details is without owner then send that details else create new one.
     */
    @Transactional
    @Override
    public CRAPIResponse sendVehicleDetails(HttpServletRequest request) {
        User user;
        UserDetails userDetails;
        VehicleDetails vehicleDetails = null;
        List<VehicleDetails> vehicleDetailList =null;
            user = userService.getUserFromToken(request);
            userDetails = userDetailsRepository.findByUser_id(user.getId());
            vehicleDetailList = vehicleDetailsRepository.findAll();
            for (VehicleDetails vehicleDetail:vehicleDetailList) {
                if (vehicleDetail.getOwner()==null) {
                    vehicleDetails = vehicleDetail;
                    break;
                }
            }
            if (vehicleDetails == null) {
                vehicleDetails = createVehicle();
            }
            smtpMailServer.sendMail(user.getEmail(), MailBody.signupMailBody(vehicleDetails,
                (userDetails!=null && userDetails.getName()!=null?userDetails.getName():"")), "Welcome to crAPI");
            return new CRAPIResponse(UserMessage.VEHICLE_DETAILS_SENT_TO_EMAIL,200);
    }

    /**
     * @return list of vehicle location,
     * fetching list of vehicle for random location se
     */
    @Transactional
    public VehicleLocation getVehicleLocationList(){
        Random random = new Random();
        List<VehicleLocation> vehicleLocation = null;
        vehicleLocation = vehicleLocationRepository.findAll();
        if (vehicleLocation!=null && !vehicleLocation.isEmpty()){
            return vehicleLocation.get(random.nextInt(vehicleLocation.size()));
        } else{
            logger.error("Location list is empty");
        }
        return new VehicleLocation("33.7967129","-84.3909149");
    }

}
