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
import com.crapi.entity.Otp;
import com.crapi.entity.User;
import com.crapi.enums.EStatus;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.ForgetPassword;
import com.crapi.model.OtpForm;
import com.crapi.model.CRAPIResponse;
import com.crapi.repository.OtpRepository;
import com.crapi.repository.UserRepository;
import com.crapi.service.OtpService;
import com.crapi.utils.MailBody;
import com.crapi.utils.OTPGenerator;
import com.crapi.utils.SMTPMailServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author Traceable AI
 */

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);

    @Autowired
    OtpRepository otpRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    SMTPMailServer smtpMailServer;

    /**
     * Service Method for invalidate OTP
     * @param {Otp} otp
     */
    @Transactional
    @Override
    public boolean invalidateOtp(Otp otp){
        try {
            otp.setStatus(EStatus.INACTIVE.toString());
            Otp saveOtp = otpRepository.save(otp);
            return true;
        }catch (Exception e){
            logger.error("Fail to invalidate otp -> Message: {}", e);
            return false;
        }


    }

    /**
     * @param otpForm
     * @return otp object, validate the otp and then reset the password of user
     */
    @Transactional
    @Override
    public CRAPIResponse validateOtp(OtpForm otpForm) {
        CRAPIResponse validateOTPResponse = null;
        User user = null;
        Otp otp = null;
        user = userRepository.findByEmail(otpForm.getEmail());
        if (user != null) {
            otp = otpRepository.findByUser(user);
             if(validateOTPAndEmail(otp,otpForm)){
                 user.setPassword(encoder.encode(otpForm.getPassword()));
                 userRepository.save(user);
                 otp.setStatus(EStatus.INACTIVE.toString());
                 validateOTPResponse = new CRAPIResponse(UserMessage.OTP_VARIFIED_SUCCESS, 200);
             }else{
                 otp.setCount(otp.getCount() + 1);
                 validateOTPResponse =  new CRAPIResponse(UserMessage.INVALID_OTP, 500);
             }
             otpRepository.save(otp);
             return validateOTPResponse;
        }
        throw new EntityNotFoundException(User.class,"userEmail", otpForm.getEmail());
    }

    @Override
    public CRAPIResponse secureValidateOtp(OtpForm otpForm) {
        CRAPIResponse crapiAPIResponse = null;
        CRAPIResponse validateOTPResponse = null;
        User user = null;
        Otp otp = null;
        user = userRepository.findByEmail(otpForm.getEmail());
        if (user!= null) {
            otp = otpRepository.findByUser(user);
            if(validateOTPAndEmail(otp, otpForm)){
                user.setPassword(encoder.encode(otpForm.getPassword()));
                userRepository.save(user);
                otp.setStatus(EStatus.INACTIVE.toString());
                validateOTPResponse = new CRAPIResponse(UserMessage.OTP_VARIFIED_SUCCESS, 200);
            }else if(otp.getCount()==9) {
                otp.setCount(otp.getCount() + 1);
                invalidateOtp(otp);
                validateOTPResponse = new CRAPIResponse(UserMessage.EXCEED_NUMBER_OF_ATTEMPS, 503);
            }else if(otp.getCount()>9) {
                otp.setCount(otp.getCount() + 1);
                validateOTPResponse = new CRAPIResponse(UserMessage.ERROR, 500);
                invalidateOtp(otp);
            }else{
                otp.setCount(otp.getCount() + 1);
                validateOTPResponse = new CRAPIResponse(UserMessage.INVALID_OTP, 500);
            }
            otpRepository.save(otp);
            return validateOTPResponse;
        }
        throw new EntityNotFoundException(User.class,"userEmail", otpForm.getEmail());

    }


    /**
     * @param forgetPassword contains user email
     * @return its generate the otp and sent email to register email
     */
    @Transactional
    @Override
    public CRAPIResponse generateOtp(ForgetPassword forgetPassword) {
        CRAPIResponse forgetPasswordResponse = null;
        OTPGenerator otpGenerator = new OTPGenerator();
        Otp checkOtpEnteryForUser = null;
        User user = null;
        String otp = "";
        user = userRepository.findByEmail(forgetPassword.getEmail());
        if (user != null) {
            //Generate random 4 digit otp
            otp = otpGenerator.generateRandom(4);
            if (otp != null) {
                //Check OTP entry for user in database.
                checkOtpEnteryForUser = otpRepository.findByUser(user);
                if (checkOtpEnteryForUser != null) {
                    //Update existing object
                    checkOtpEnteryForUser.setCount(0);
                    checkOtpEnteryForUser.setOtp(otp);
                } else {
                    //Create new OTP object
                    checkOtpEnteryForUser = new Otp(otp, user);
                }
                checkOtpEnteryForUser.setStatus(EStatus.ACTIVE.toString());
                //Save otp details in Database.
                otpRepository.save(checkOtpEnteryForUser);
                //Sent OTP mail to user email address.
                smtpMailServer.sendMail(user.getEmail(), MailBody.otpMailBody(checkOtpEnteryForUser), "crAPI OTP");
                return new CRAPIResponse(UserMessage.OTP_SEND_SUCCESS_ON_EMAIL + user.getEmail(),200);
            }
            throw new RuntimeException();

        }
        return new CRAPIResponse(UserMessage.EMAIL_NOT_REGISTERED+forgetPassword.getEmail(),404);
    }

    /**
     * @param otp
     * @return
     */
    public boolean validateOTPAndEmail(Otp otp,OtpForm otpForm) {
        if (otp != null) {
            if (otp.getStatus().equalsIgnoreCase(EStatus.ACTIVE.toString()) &&
                    otp.getOtp().equalsIgnoreCase(otpForm.getOtp()) &&
                        otp.getUser().getEmail().equalsIgnoreCase(otpForm.getEmail())) {
                return true;
            }
            return false;
        }
        throw new EntityNotFoundException(Otp.class,"OTP Details Not Found",otpForm.getEmail());
    }
}
