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

import com.crapi.config.JwtProvider;
import com.crapi.constant.UserMessage;
import com.crapi.model.DashboardResponse;
import com.crapi.model.LoginForm;
import com.crapi.model.CRAPIResponse;
import com.crapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;


/**
 * @author Traceable AI
 */
@CrossOrigin
@RestController
@RequestMapping("/identity/api/v2/user")
public class UserController {



    @Autowired
    UserService userService;

    @Autowired
    private JwtProvider tokenProvider;


    /**
     * @param request getting jwt token for user from request header
     * @return user object with the details of vehicle and profile by token email.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpServletRequest request){
        DashboardResponse userData = userService.getUserByRequestToken(request);
       if (userData!=null) {
           return ResponseEntity.status(HttpStatus.OK).body(userData);
       }else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CRAPIResponse
                    (UserMessage.EMAIL_NOT_REGISTERED,404));
    }
  
    /**
     * @param loginForm contains email and updated password
     * @param request getting jwt token for user from request header
     * @return reset user password for the user. first verify token and then reset user password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<CRAPIResponse> resetPassword(@RequestBody LoginForm loginForm, HttpServletRequest request) throws UnsupportedEncodingException {

        CRAPIResponse resetPasswordResponse = userService.resetPassword(loginForm, request);
        if (resetPasswordResponse!=null && resetPasswordResponse.getStatus()==200) {
            return ResponseEntity.ok().body(new CRAPIResponse(UserMessage.PASSWORD_GOT_RESET));
        }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resetPasswordResponse);
    }

}
