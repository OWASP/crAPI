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

package com.crapi.controller;


import com.crapi.constant.UserMessage;
import com.crapi.model.*;
import com.crapi.service.OtpService;
import com.crapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;


/**
 * @author Traceable AI
 */

@CrossOrigin
@RestController
@RequestMapping("/identity/api/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    OtpService otpService;


    /**
     * @param loginForm contains user email and password for login
     * @return getting jwt token of user from request header
     * @throws UnsupportedEncodingException throws UnsupportedEncodingException for password encryption
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginForm loginForm) throws UnsupportedEncodingException {
        JwtResponse jwtToken = userService.authenticateUserLogin(loginForm);
        if (jwtToken!=null && jwtToken.getToken()!=null) {
            return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jwtToken);
        }
    }


    /**
     * @param signUpRequest contains user email,number,name and password
     * @return success and failure message after user registration.
     */
    @PostMapping("/signup")
    public ResponseEntity<CRAPIResponse> registerUser(@Valid @RequestBody SignUpForm signUpRequest){
        // Creating user's account
        CRAPIResponse registerUserResponse = userService.registerUser(signUpRequest);
        if (registerUserResponse!=null && registerUserResponse.getStatus()==200){
            return ResponseEntity.status(HttpStatus.OK).body(registerUserResponse);
        }
        else if (registerUserResponse!=null && registerUserResponse.getStatus()==403){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(registerUserResponse);
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerUserResponse);
        }
    }


    /**
     * @param forgetPassword contains user email for which user want to generate otp
     * @return success and failure message after generating otp
     * and sent the otp to the register email.
     */
    @PostMapping("/forget-password")
    public ResponseEntity<CRAPIResponse> forgetPassword(@Valid @RequestBody ForgetPassword forgetPassword){
        CRAPIResponse forgetPasswordResponse =otpService.generateOtp(forgetPassword);
        if (forgetPasswordResponse!=null && forgetPasswordResponse.getStatus()==200){
            return ResponseEntity.status(HttpStatus.OK).body(forgetPasswordResponse);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(forgetPasswordResponse);
    }

    /**
     * @param otpForm contains otp, updated password and user email
     * @return success and failure response
     * its non secure API for attacker. in this attacker can enter 'n' number of times invalid otp
     */
    @PostMapping("/v2/check-otp")
    public ResponseEntity<CRAPIResponse> checkOtp(@RequestBody OtpForm otpForm) {
        CRAPIResponse validateOtpResponse = otpService.validateOtp(otpForm);
        if (validateOtpResponse!=null && validateOtpResponse.getStatus()==200) {
            return ResponseEntity.status(HttpStatus.OK).body(validateOtpResponse);
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validateOtpResponse);
        }

    }

    /**
     * @param otpForm contains otp, updated password and user email
     * @return success and failure response
     * its secure otp validator in this user can enter 10 times invalid otp
     * after 10 invalid otp it will invalidate the otp.
     */
    @PostMapping("/v3/check-otp")
    public ResponseEntity<CRAPIResponse> secureCheckOtp(@RequestBody OtpForm otpForm){
        CRAPIResponse validateOtpResponse = otpService.secureValidateOtp(otpForm);
        if (validateOtpResponse.getStatus()==200) {
            return ResponseEntity.status(HttpStatus.OK).body(validateOtpResponse);
        }else if(validateOtpResponse.getStatus()==503) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(validateOtpResponse);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validateOtpResponse);
    }

    /**
     * @param loginWithEmailToken contains user email and email change token, which allow user login with email token
     * @return double verification message
     */
    @PostMapping("/v4.0/user/login-with-token")
    public ResponseEntity<CRAPIResponse> loginWithToken(@RequestBody LoginWithEmailToken loginWithEmailToken){
        CRAPIResponse response=userService.loginWithEmailToken(loginWithEmailToken);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * @param loginWithEmailToken contains user email and email change token, which allow user login with email token
     * @return jwt token for login with token
     */
    @PostMapping("/v2.7/user/login-with-token")
    public ResponseEntity<JwtResponse> loginWithTokenV2(@Valid @RequestBody LoginWithEmailToken loginWithEmailToken){
        JwtResponse jwt = userService.loginWithEmailTokenV2(loginWithEmailToken);
        if (jwt.getToken()!=null && jwt.getToken().length()>5){
            return ResponseEntity.status(HttpStatus.OK).body(jwt);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jwt);
    }

    

}
